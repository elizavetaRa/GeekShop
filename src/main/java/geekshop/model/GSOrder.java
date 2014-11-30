package geekshop.model;

import javax.persistence.*;

import org.salespointframework.order.Order;
import org.salespointframework.order.OrderLine;
import org.salespointframework.payment.PaymentMethod;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.util.Assert;

/**
 * Created by Lisa on 30.11.2014.
 */

@Entity
public class GSOrder extends Order{
    @Id
    private UserAccount ua;
    @Enumerated(EnumType.STRING)
    private OrderType type;
    private PaymentMethod paymentMethod;
    private OrderLine orderLine;
    private OrderLineState ols;



    @Deprecated
    protected GSOrder() {
    }

    public GSOrder(UserAccount ua, OrderType type) {
      /*Assert.notNull(ua, "UserAccount must not be null.");
      Assert.notNull(type, "OrderType must not be null.");*/
      this.ua = ua;
      this.type=type;

    }

    public GSOrder(UserAccount ua, OrderType type, PaymentMethod paymentMethod) {
        /*Assert.notNull(ua, "UserAccount must not be null.");
        Assert.notNull(type, "OrderType must not be null.");
        Assert.notNull(paymentMethod, "PaymentMethod must not be null.");*/
        this.ua = ua;
        this.type=type;
        this.paymentMethod=paymentMethod;

    }

    public void setOrderType (OrderType type){this.type=type;}

    public OrderType getOrderType (){return type; }




}
