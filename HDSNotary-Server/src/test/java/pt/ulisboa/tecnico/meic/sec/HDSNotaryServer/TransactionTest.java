package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TransactionTest {

    private User seller = new User(12, 1);
    private User buyer = new User(14,2);
    private Good good;
    private Transaction transaction;

    private static final String PENDING_STATE = "Pending";
    private static final String APPROVED_STATE = "Approved";
    private static final String REJECT_STATE = "Reject";
    private static final String CANCELLED_STATE = "Cancelled";

    @Before
    public void setUp() throws  GoodException{
        good = new Good(1, seller, true);
        transaction = new Transaction(1, seller, buyer, good);
    }

    @Test
    public void simpleTransaction() throws GoodException {

        assertNotNull(transaction);

        assertEquals(1, transaction.getTransactionID());
        assertEquals(buyer, transaction.getBuyer());
        //assertEquals(good, transaction.getGood());
        assertEquals(PENDING_STATE, transaction.getTransactionStateDescription());
    }

    @Test
    public void transactionAccepted() {

        assertEquals(PENDING_STATE, transaction.getTransactionStateDescription());
        transaction.execute();
        assertEquals(APPROVED_STATE, transaction.getTransactionStateDescription());

    }

    @Test
    public void transactionWithItemTheft() {
        User eve = new User(10, 9);
        Transaction fraud = new Transaction(2, eve, buyer, good);
        assertEquals(PENDING_STATE, fraud.getTransactionStateDescription());
        fraud.execute();
        assertEquals(REJECT_STATE, fraud.getTransactionStateDescription());

    }

    @Test
    public void transactionWithItemNotForSale() throws GoodException {
        Good notForSale = new Good(2, seller, false);
        Transaction fraud = new Transaction(2, seller, buyer, notForSale);
        assertEquals(PENDING_STATE, fraud.getTransactionStateDescription());
        fraud.execute();
        assertEquals(REJECT_STATE, fraud.getTransactionStateDescription());

    }

    @Test
    public void transactionOwnershipChange() {
        User currentOwner = transaction.getGood().getOwner();

        assertEquals(PENDING_STATE, transaction.getTransactionStateDescription());
        transaction.execute();
        assertEquals(APPROVED_STATE, transaction.getTransactionStateDescription());
        transaction.execute();
        assertEquals(APPROVED_STATE, transaction.getTransactionStateDescription());

        assertNotNull(transaction.getGood().getOwner());

        assertNotEquals(currentOwner, transaction.getGood().getOwner());
    }

    /**
     * When two transactions start with the same good as different buyers then the second one should be cancelled
     */
    @Test
    public void twoTransactionSameGood() {
        User anotherBuyer = new User(3, 23);
        Transaction t2 = new Transaction(2, seller, anotherBuyer, good);

        assertEquals(PENDING_STATE, transaction.getTransactionStateDescription());
        assertEquals(PENDING_STATE, t2.getTransactionStateDescription());

        transaction.execute();
        t2.execute();

        assertEquals(APPROVED_STATE, transaction.getTransactionStateDescription());
        assertEquals(CANCELLED_STATE, t2.getTransactionStateDescription());
    }

}