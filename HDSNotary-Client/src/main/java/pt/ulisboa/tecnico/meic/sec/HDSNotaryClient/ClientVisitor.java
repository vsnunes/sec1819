package pt.ulisboa.tecnico.meic.sec.HDSNotaryClient;

/**
 * Visitor interface to display result of operations on the client
 */
public interface ClientVisitor {

    /** checking for failures in operations **/
    boolean check4Failures(Operation operation);

    void accept(BuyGood operation);
    void accept(GetStateOfGood operation);
    void accept(IntentionToSell operation);
    void accept(TransferGood operation);
    void accept(Debug operation);
}
