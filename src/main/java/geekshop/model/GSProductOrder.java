package geekshop.model;

/*
 * Created by Basti on 08.12.2014.
 */

import org.springframework.util.Assert;

import java.time.LocalDateTime;

/**
 * A helper class to encapsulate a {@link GSOrderLine} related to a specific {@link org.salespointframework.catalog.Product} as well as the date sold and the seller.
 *
 * @author Sebastian D&ouml;ring
 */

public class GSProductOrder {

    public GSOrderLine orderLine;
    public LocalDateTime date;
    public User seller;


    @Deprecated
    protected GSProductOrder() {
    }

    public GSProductOrder(GSOrderLine orderLine, LocalDateTime date, User seller) {
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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }
}
