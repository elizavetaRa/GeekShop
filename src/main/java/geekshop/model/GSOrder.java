package geekshop.model;

/*
 * Created by Lisa on 30.11.2014.
 */

import org.salespointframework.core.SalespointIdentifier;
import org.salespointframework.order.Order;
import org.salespointframework.payment.PaymentMethod;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;

/**
 * An extension of {@link Order} extended by {@link OrderType}.
 *
 * @author Elizaveta Ragozina
 * @author Sebastian D&ouml;ring
 */

@Entity
public class GSOrder extends Order {

    private final SalespointIdentifier orderNumber;

    @Enumerated(EnumType.STRING)
    private OrderType type;

    @OneToOne
    private final GSOrder reclaimedOrder;


    @Deprecated
    protected GSOrder() {
        this.orderNumber = new SalespointIdentifier();
        this.reclaimedOrder = null;
    }

    public GSOrder(String orderNumber, UserAccount ua, OrderType type) {
        super(ua);
        initializeGSOrder(type);

        this.orderNumber = new SalespointIdentifier(orderNumber);
        this.reclaimedOrder = null;
    }

    public GSOrder(String orderNumber, UserAccount ua, OrderType type, GSOrder reclaimedOrder) {
        super(ua);
        initializeGSOrder(type);

        this.orderNumber = new SalespointIdentifier(orderNumber);
        this.reclaimedOrder = reclaimedOrder;
    }

    public GSOrder(String orderNumber, UserAccount ua, OrderType type, PaymentMethod paymentMethod) {
        super(ua, paymentMethod);
        initializeGSOrder(type);

        this.orderNumber = new SalespointIdentifier(orderNumber);
        this.reclaimedOrder = null;
    }

    public GSOrder(String orderNumber, UserAccount ua, OrderType type, PaymentMethod paymentMethod, GSOrder reclaimedOrder) {
        super(ua, paymentMethod);
        initializeGSOrder(type);

        this.orderNumber = new SalespointIdentifier(orderNumber);
        this.reclaimedOrder = reclaimedOrder;
    }

    private void initializeGSOrder(OrderType type) {
        Assert.notNull(type, "OrderType must not be null.");
        this.type = type;
    }


    public SalespointIdentifier getOrderNumber() {
        return orderNumber;
    }

    public void setOrderType(OrderType type) {
        this.type = type;
    }

    public OrderType getOrderType() {
        return type;
    }

    public GSOrder getReclaimedOrder() {
        return reclaimedOrder;
    }
}
