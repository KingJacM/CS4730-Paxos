import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

class Peer implements Runnable {
    private int peerId;
    // private int sequenceNumber = 0; // Local counter for proposals

    private String myName;
    private boolean testcase2;
    private ServerSocket serverSocket;
    private ExecutorService executor;
    private boolean isProposer = false;
    private boolean isAcceptor = false;
    private boolean isLearner  = false;
    private volatile int minProposal = 0;
    private volatile Proposal acceptedProposal;
    private int currentProposalId;
    private Map<Integer, Integer> promiseCounters = new HashMap<>() ;
    private Map<Integer, Integer> acceptedCounters = new HashMap<>() ;// To count promises for each proposal
    private Map<Integer, Proposal> sentProposals = new HashMap<>(); // To keep track of sent proposals
    private List<String> peersList;
    private BlockingQueue<Message> messageQueue;
    private String value;

    public Peer(int peerId, String name, List<String> peersList) throws IOException {
        this.peerId = peerId;
        this.myName = name;
        this.serverSocket = new ServerSocket();
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.peersList = peersList;
        this.messageQueue = new LinkedBlockingQueue<>();
        this.serverSocket = new ServerSocket(1234);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setTestcase2(boolean testcase2) {
        this.testcase2 = testcase2;
    }

    public void setAcceptor(boolean acceptor) {
        isAcceptor = acceptor;
    }
    public void setLearner(boolean learner) {
        isLearner = learner;
    }
    public void setProposer(boolean proposer) {
        isProposer = proposer;
    }

    public synchronized int nextProposalNumber() {
        int timestamp = (int) System.currentTimeMillis();
        return timestamp + peerId; // Construct unique proposal number
    }

    public int getPeerId() {
        return peerId;
    }

    public void run() {
        if (isProposer){
            int time = 1000;
            if (testcase2 == true){
                time = 4000;
            }
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            Propose(value);
        }

        // Start a thread to listen for incoming connections
        executor.submit(this::listenForConnections);

        // Main loop to process messages
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Message message = messageQueue.take(); // Blocks until a message is available
                processMessage(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void listenForConnections() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket clientSocket = serverSocket.accept();
                executor.submit(() -> handleConnection(clientSocket));
            } catch (IOException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void handleConnection(Socket clientSocket) {
        try (ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream())) {
            Message message = (Message) input.readObject();
            messageQueue.offer(message); // Add the message to the queue for processing
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void processMessage(Message message) {
        System.out.println("Message Recieved: " + message);
        switch (message.getStatus()) {
            case "Prepare":
                handlePrepare(message);
                break;
            case "Accept":
                handleAccept(message);
                break;
            case "Promise":
                handlePromise(message);
                break;
            case "Accepted":
                handleAccepted(message);
                break;
        }
    }

//    private void handleAccepted(Message message) {
//        int proposalId = message.getProposalId();
//        Proposal receivedProposal = message.getProposal();
//        // Check if the received proposal has a value and if it's greater than the current proposal's value
//        Proposal currentProposal = sentProposals.get(proposalId);
//
//        if (receivedProposal.getValue().compareTo(currentProposal.getValue()) > 0) {
//            // Update the proposal's value with the received value
//            Propose(receivedProposal.getValue());
//            return;
//        }
//
//        // Increment the count of promises received for this proposal
//        acceptedCounters.put(proposalId, promiseCounters.getOrDefault(proposalId, 0) + 1);
//
//        // Check if received promises are over the majority
//        if (promiseCounters.get(proposalId) > peersList.size() / 2) {
////            if ((currentProposal == null || receivedProposal.getProposalId() > (currentProposal.getProposalId()))) {
////                // Update the proposal's value with the received value
////                Propose();
////            }
////             else {
//                // Majority of promises received, proceed to broadcast the accept request
//                acceptedProposal = currentProposal;
//                System.out.println("Alright folks, its final, everyone accepted "+acceptedProposal);
//            //}
//        }
//    }
private void handleAccepted(Message message) {
    int proposalId = message.getProposalId();
    if (currentProposalId != proposalId){
        return;
    }
    Proposal receivedProposal = message.getProposal();

    // Ensure the received proposal is for the current proposal
    if (proposalId == this.currentProposalId) {
        // Increment the count of accepted responses for this proposal
        acceptedCounters.put(proposalId, acceptedCounters.getOrDefault(proposalId, 0) + 1);

        // Check if received accepted responses are over the majority
        if (acceptedCounters.get(proposalId) > peersList.size() / 2) {
            // Majority of acceptances received, the value is chosen
            acceptedProposal = receivedProposal;
            currentProposalId = -1;
            System.out.println("Consensus reached on value: " + acceptedProposal.getValue());
        }
    } else if (proposalId > this.currentProposalId) {
        // A more recent proposal exists, may need to restart the process
        System.out.println("More recent proposal detected, restarting...");
        // Implement logic to restart the prepare phase
    }
}


//    private void handleAccept(Message message) {
//        int proposalId = message.getProposalId();
//        Proposal proposal = message.getProposal();
//
//        if (this.isAcceptor) {
//            // Check if the proposal number is greater than or equal to minProposal
//            if (proposalId >= this.minProposal) {
//                // Accept the proposal
//                acceptedProposal = proposal;
//                sendMessage(new Message("Accepted", proposal, proposal.getProposalId(),myName), message.getSenderName()); // Notify others that the proposal is accepted
//                System.out.println("If nothing goes wrong, I accept "+proposal+" here.");
//            } else {
//                // Proposal number is less than minProposal, reject the proposal
//                // Optionally, could notify the proposer that their proposal was rejected
//                // This could be a message with the current minProposal to help the proposer adjust
//
//                // NOTE: status should be "REJECTED", "Accepted" in this case is a bit confusing
//                System.out.println("Sorry I have to reject to accept "+proposal);
//                sendMessage(new Message("Accepted", acceptedProposal, minProposal, myName), message.getSenderName()); // Method to be implemented
//            }
//        }
//    }

    private void handleAccept(Message message) {
        int proposalId = message.getProposalId();
        Proposal proposal = message.getProposal();

        if (this.isAcceptor) {
            if (proposalId >= this.minProposal) {
                // Accept the proposal
                acceptedProposal = proposal;
                System.out.println("If nothing goes wrong, I accept " + proposal + " here.");
                sendMessage(new Message("Accepted", proposal, proposal.getProposalId(), myName), message.getSenderName());
            } else {
                // Proposal number is less than minProposal, reject the proposal
                System.out.println("Sorry, I have to reject to accept " + proposal);
                sendMessage(new Message("Rejected", null, minProposal, myName), message.getSenderName());
            }
        }
    }

//    private void handlePromise(Message message) {
//        int proposalId = message.getProposalId();
//        Proposal receivedProposal = message.getProposal();
//
//        // Increment the count of promises received for this proposal
//        promiseCounters.put(proposalId, promiseCounters.getOrDefault(proposalId, 0) + 1);
//
//        // Check if the received proposal has a value and if it's greater than the current proposal's value
//        Proposal currentProposal = sentProposals.get(proposalId);
//
//        if (receivedProposal != null && receivedProposal.getValue().compareTo(currentProposal.getValue()) > 0) {
//            // Update the proposal's value with the received value
//            currentProposal.setValue(receivedProposal.getValue()); // issue here?
//            value = receivedProposal.getValue();
//        }
//
//        // Check if received promises are over the majority
//        if (promiseCounters.get(proposalId) > peersList.size() / 2){
//                // Majority of promises received, proceed to broadcast the accept request
//                broadcastAccept(currentProposal);
//        }
//    }

    private void handlePromise(Message message) {
        int proposalId = message.getProposalId();
        if (currentProposalId != proposalId){
            return;
        }
        Proposal receivedProposal = message.getProposal();

        // Increment the count of promises received for this proposal
        promiseCounters.put(proposalId, promiseCounters.getOrDefault(proposalId, 0) + 1);

        Proposal currentProposal = sentProposals.get(proposalId);

        if (receivedProposal != null) {
            System.out.println("changed value to "+ receivedProposal.getValue());
            currentProposal.setValue(receivedProposal.getValue());

        }

        // Check if received promises are over the majority
        if (promiseCounters.get(proposalId) > peersList.size() / 2) {
            // Majority of promises received, proceed to broadcast the accept request
            broadcastAccept(currentProposal);
        }
    }


    private void broadcastAccept(Proposal currentProposal) {
        broadcast(new Message("Accept", currentProposal, currentProposal.getProposalId(),myName));
    }


    private void handlePrepare(Message message){
        if(message.getProposalId() > minProposal){
            minProposal = message.getProposalId();
            sendPromise(message.getProposalId(),message.getSenderName(), acceptedProposal);
        }
        else{
            System.out.println("Sorry I have to reject because message.getProposalId() > minProposal: "+ message.getProposalId());
        }
    }

    public void broadcast(Message message){
        // System.out.print("entering broadcasting \n");
        for (int i = 0; i < this.peersList.size();i++){
            Socket socket = null;
            if (!this.peersList.get(i).equals(this.myName)){ // skip myself
                try {
                    System.out.println(this.peersList.get(i));
                    socket = new Socket(this.peersList.get(i), 1234);

                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectOutputStream.writeObject(message);
                    objectOutputStream.close();
                    socket.close();
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private Proposal Propose(String value){
        Proposal prop = new Proposal(nextProposalNumber(), value);
        currentProposalId = prop.getProposalId();
        Message message = new Message("Prepare", prop, prop.getProposalId(), myName);
        broadcast(message);
        sentProposals.put(prop.getProposalId(),prop);
        return prop;
    }



    private void sendPromise(int currentCaseId, String targetName, Proposal acceptedProposal) {
        Message message = new Message("Promise",acceptedProposal, currentCaseId,myName );
        sendMessage(message, targetName);
    }

    // Additional methods for Prepare, Propose, Accept, Promise, etc.

    // Method to send messages to other peers using TCP
    private void sendMessage(Message message, String recipient) {
        System.out.println("Message Sent to " + recipient + " content: " + message);
        try (Socket socket = new Socket(recipient, 1234);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream())) {
            output.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


