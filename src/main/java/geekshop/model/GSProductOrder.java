package geekshop.model;

import org.springframework.util.Assert;

import java.util.Date;

/**
 * A helper class to encapsulate a {@link GSOrderLine} related to a specific {@link org.salespointframework.catalog.Product} as well as the date sold and the seller.
 *
 * @author Sebastian D&ouml;ring
 */

public class GSProductOrder {

    private GSOrderLine orderLine;
    private Date date;
    private User seller;


    @Deprecated
    protected GSProductOrder() {
    }

    public GSProductOrder(GSOrderLine orderLine, Date date, User seller) {
        Assert.notNull(orderLine, "OrderLine must not be null!");
        Assert.notNull(date, "Date must not be null!");
        Assert.notNull(seller, "Seller must not be null!");

        this.orderLine = orderLine;
        this.date = date;
        this.seller = seller;
    }


    public GSOrderLine getOrderLine() {
        return orderLine;
    }

    public void setOrderLine(GSOrderLine orderLine) {
        this.orderLine = orderLine;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }
}
