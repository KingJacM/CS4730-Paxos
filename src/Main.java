import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws UnknownHostException {
//        System.out.println("Hello world!");

//        peer1:proposer1
//        peer2:acceptor1
//        peer3:acceptor1
//        peer4:acceptor1
//        peer5:learner1
        int id = 0;
        String[] roles = new String[3];
        List<String> hostNames = new ArrayList<>();
        String myName = InetAddress.getLocalHost().getHostName();
        //change filename here

        String value = null;
        String fileName = null;
        boolean testcase2 = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-v":
                    if (i + 1 < args.length) {
                        value = args[i + 1];
                        System.out.println(value);
                        i++;  // Increment to skip next argument
                    }
                    break;
                case "-t":
                    testcase2 = true;
                    break;
                case "-h":
                    if (i + 1 < args.length) {
                        fileName = args[i + 1];
                        i++;  // Increment to skip next argument
                    }
                    break;


            }
        }
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int i = 1;

            while ((line = br.readLine()) != null) {
                String[] arrSplit = line.split(":");
                String peerName = arrSplit[0];
//                System.out.println(peerName);
//                System.out.println(arrSplit[1].split(","));

                if(myName.equals(peerName)){
                    id = i;
                    roles = arrSplit[1].split(",");
                }
                hostNames.add(peerName);
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        
//        System.out.println(testcase2);

        try {
            // Create a new Peer using the hostNames

            Peer currentPeer = new Peer(id, myName, hostNames);
            for(String role : roles){
                System.out.println(role);
                if(role.equals("proposer1") || role.equals("proposer2")){
                    currentPeer.setProposer(true);
                    System.out.println("I am "+role);
                    if (testcase2 == true && role.equals("proposer2")) {
                        currentPeer.setTestcase2(true);
                    }
                }
                if(role.equals("acceptor1")){
                    currentPeer.setAcceptor(true);
                }
                if(role.equals("learner1")){
                    currentPeer.setLearner(true);
                }
            }

            if(value!=null){
                currentPeer.setValue(value);
            }

            Thread peerThread = new Thread(currentPeer);
            peerThread.start();


        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.err.println("Failed to initialize the peer due to an unknown host exception.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}