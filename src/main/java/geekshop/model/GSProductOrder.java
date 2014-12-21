package geekshop.model;

import org.salespointframework.payment.Cheque;
import org.salespointframework.payment.CreditCard;
import org.salespointframework.payment.PaymentMethod;
import org.springframework.util.Assert;

import java.util.Date;

/**
 * A helper class to encapsulate a {@link GSOrderLine} related to a specific {@link org.salespointframework.catalog.Product} as well as the date sold and the seller.
 *
 * @author Sebastian D&ouml;ring
 */

public class GSProductOrder implements Comparable<GSProductOrder> {

    private GSOrderLine orderLine;
    private Date date;
    private long orderNumber;
    private PaymentType paymentType;
    private User seller;


    @Deprecated
    protected GSProductOrder() {
    }

    public GSProductOrder(GSOrderLine orderLine, Date date, long orderNumber, PaymentMethod paymentMethod, User seller) {
        Assert.notNull(orderLine, "OrderLine must not be null!");
        Assert.notNull(date, "Date must not be null!");
        Assert.notNull(paymentMethod, "PaymentMethod must not be null!");
        Assert.notNull(seller, "Seller must not be null!");

        this.orderLine = orderLine;
        this.date = date;
        this.orderNumber = orderNumber;

        if (paymentMethod instanceof Cheque)
            this.paymentType = PaymentType.CHEQUE;
        else if (paymentMethod instanceof CreditCard)
            this.paymentType = PaymentType.CREDITCARD;
        else
            this.paymentType = PaymentType.CASH;

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

    public long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(long orderNumber) {
        this.orderNumber = orderNumber;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    @Override
    public int compareTo(GSProductOrder other) {
        return this.date.compareTo(other.date);
    }
}
