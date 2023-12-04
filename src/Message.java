import java.io.Serializable;

class Message implements Serializable {
    private String status;
    private Proposal proposal;
    private int currentProposalId;
    private String senderName;


    public Message(String status,Proposal proposal, int proposalId, String senderName ){
        this.status = status;
        this.proposal = proposal;
        this.currentProposalId = proposalId;
        this.senderName = senderName;
    }


    public String getStatus() {
        return status;
    }

    public int getProposalId() {
        return currentProposalId;
    }

    public Proposal getProposal() {
        return proposal;
    }

    public String getSenderName() {
        return senderName;
    }

    @Override
    public String toString() {
        return "Message{" +
                "status='" + status + '\'' +
                ", proposal=" + (proposal != null ? proposal.toString() : "null") +
                ", proposalId=" + currentProposalId +
                ", senderName='" + senderName + '\'' +
                '}';
    }

}
