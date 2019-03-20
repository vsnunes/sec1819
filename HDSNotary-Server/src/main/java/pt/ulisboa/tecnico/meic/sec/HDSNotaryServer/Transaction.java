package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

/**
 * A Class for describing Transactions of goods beetween two
 */
public class Transaction {

    private int transactionID;

    private User seller;

    private User buyer;

    private Good good;

    private TransactionState state;

    public Transaction(int transactionID, User seller, User buyer, Good good) {
        this.transactionID = transactionID;
        this.seller = seller;
        this.buyer = buyer;
        this.good = good;
        this.state = new PendingState();
    }

    public int getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public Good getGood() {
        return good;
    }

    public void setGood(Good good) {
        this.good = good;
    }

    public TransactionState getState() {
        return state;
    }

    protected void setState(TransactionState state) {
        this.state = state;
    }

    public String getTransactionStateDescription() {
        return this.state.getStateName();
    }

    public void execute() {
        this.state.execute(this);
    }
}
