package pt.ulisboa.tecnico.meic.sec.HDSNotaryBadClient;

/**
 * Visitor interface to display result of operations on the client
 */
public interface ClientVisitor {

    /** checking for failures in operations **/
    boolean check4Failures(Operation operation);

    void accept(BuyGoodTampered operation);
    void accept(BuyGoodReplay operation);
    void accept(GetStateOfGoodReplay operation);
    void accept(IntentionToSellAlteredKey operation);
    void accept(IntentionToSellTampered operation);
    void accept(IntentionToSellReplay operation);
    void accept(TransferGoodTampered operation);
    void accept(TransferGoodReplay operation);
    void accept(Debug operation);
}
