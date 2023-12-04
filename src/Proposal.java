import java.io.Serializable;

class Proposal implements Serializable {
    private int proposalId;
    private String value;

    public Proposal(int proposalId, String value){
        this.proposalId = proposalId;
        this.value= value;
    }

    public int getProposalId() {
        return proposalId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Proposal{" +
                "proposalId=" + proposalId +
                ", value=" + value +
                '}';
    }

    // Constructor and getters/setters
}
