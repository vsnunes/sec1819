package pt.ulisboa.tecnico.meic.sec.HDSNotaryBadClient;

/**
 * Visitor interface to display result of operations on the client
 */
public interface ClientVisitor {

    /** checking for failures in operations **/
    boolean check4Failures(Operation operation);

    void accept(BuyGoodWithoutPoW operation);
    void accept(GetStateOfGood operation);
    void accept(IntentionToSell operation);
    void accept(TransferGood operation);
    void accept(Debug operation);
    void accept(IntentionToSellTo2 operation);
    void accept(IntentionToSellTo3 operation);
    void accept(IntentionToSellReplay operation);
    void accept(IntentionToSellTampered operation);
}
