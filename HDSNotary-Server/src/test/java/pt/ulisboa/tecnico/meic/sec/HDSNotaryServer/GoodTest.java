package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import org.junit.Assert;
import org.junit.Test;

public class GoodTest {

    private User user = new User("user");

    @Test
    public void setForSellGood() throws GoodException {
        Good g = new Good(14, this.user);
        Assert.assertEquals(false, g.isForSell());

        g.setForSell(true);

        Assert.assertEquals(true, g.isForSell());
    }

    @Test
    public void GoodConstructorWithBoth() throws GoodException {
        Good g = new Good(100, this.user, true);
        Assert.assertEquals(100, g.getGoodID());
        Assert.assertEquals(true, g.isForSell());
    }

    @Test
    public void GoodDefaultConstructor() throws GoodException {
        Good g = new Good(45, this.user, true);

        Assert.assertNotNull(g);
        Assert.assertNotNull(g.getGoodID());
        Assert.assertNotNull(g.isForSell());

        Assert.assertEquals(false, g.isForSell());
    }

    @Test
    public void GoodSetterID() throws GoodException {
        Good g = new Good(4, this.user, true);
        Assert.assertEquals(4, g.getGoodID());

        g.setGoodID(8);

        Assert.assertEquals(8, g.getGoodID());
    }

    @Test
    public void setOwnerGood() throws GoodException {
        Good g = new Good(4, this.user, true);

        Assert.assertNotNull(g.getOwner());

        g.setOwner(new User("alice"));

        Assert.assertEquals(g.getOwner());

        Assert.assertEquals("alice", g.getOwner().getUserName());

    }

}