package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import org.junit.Assert;
import org.junit.Test;

public class GoodTest {
    @Test
    public void simpleGood() throws GoodException {
        Good g = new Good(12);

        Assert.assertEquals(12, g.getGoodID());
        Assert.assertEquals(false, g.isForSell());
    }

    @Test(expected = GoodException.class)
    public void invalidIDGood() throws GoodException{
        Good g = new Good(-2);
    }

    @Test
    public void setForSellGood() throws GoodException {
        Good g = new Good(14);
        Assert.assertEquals(false, g.isForSell());

        g.setForSell(true);

        Assert.assertEquals(true, g.isForSell());
    }

    @Test
    public void GoodConstructorWithBoth() throws GoodException {
        Good g = new Good(100, true);
        Assert.assertEquals(100, g.getGoodID());
        Assert.assertEquals(true, g.isForSell());
    }

    @Test
    public void GoodDefaultConstructor() throws GoodException {
        Good g = new Good();

        Assert.assertNotNull(g);
        Assert.assertNotNull(g.getGoodID());
        Assert.assertNotNull(g.isForSell());

        Assert.assertEquals(false, g.isForSell());
    }

    @Test
    public void GoodSetterID() throws GoodException {
        Good g = new Good(4,true);
        Assert.assertEquals(4, g.getGoodID());

        g.setGoodID(8);

        Assert.assertEquals(8, g.getGoodID());
    }

}