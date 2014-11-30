package geekshop.model;

import org.salespointframework.order.Order;
import org.salespointframework.payment.PaymentMethod;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Created by Lisa on 30.11.2014.
 */

@Entity
public class GSOrder extends Order {

    @Enumerated(EnumType.STRING)
    private OrderType type;


    @Deprecated
    protected GSOrder() {
    }

    public GSOrder(UserAccount ua, OrderType type) {
        super(ua);
        Assert.notNull(type, "OrderType must not be null.");
        this.type = type;
    }

    public GSOrder(UserAccount ua, OrderType type, PaymentMethod paymentMethod) {
        super(ua, paymentMethod);
        Assert.notNull(type, "OrderType must not be null.");
        this.type = type;
    }

    public void setOrderType(OrderType type) {
        this.type = type;
    }

    public OrderType getOrderType() {
        return type;
    }

}
