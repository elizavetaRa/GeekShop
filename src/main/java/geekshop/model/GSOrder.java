package geekshop.model;

import org.salespointframework.core.SalespointIdentifier;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderLine;
import org.salespointframework.payment.PaymentMethod;
import org.salespointframework.useraccount.UserAccount;

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

    public GSOrder(String orderNumber, UserAccount ua) {
        super(ua);

        this.orderNumber = new SalespointIdentifier(orderNumber);
        this.reclaimedOrder = null;
        initializeGSOrder(reclaimedOrder);
    }

    public GSOrder(String orderNumber, UserAccount ua, GSOrder reclaimedOrder) {
        super(ua);

        this.orderNumber = new SalespointIdentifier(orderNumber);
        this.reclaimedOrder = reclaimedOrder;
        initializeGSOrder(reclaimedOrder);
    }

    public GSOrder(String orderNumber, UserAccount ua, PaymentMethod paymentMethod) {
        super(ua, paymentMethod);

        this.orderNumber = new SalespointIdentifier(orderNumber);
        this.reclaimedOrder = null;
        initializeGSOrder(reclaimedOrder);
    }

    public GSOrder(String orderNumber, UserAccount ua, PaymentMethod paymentMethod, GSOrder reclaimedOrder) {
        super(ua, paymentMethod);

        this.orderNumber = new SalespointIdentifier(orderNumber);
        this.reclaimedOrder = reclaimedOrder;
        initializeGSOrder(reclaimedOrder);
    }

    private void initializeGSOrder(GSOrder reclaimedOrder) {

        ////////////////////////////////////// Validierung! ////////////////////////////////////////

        if (reclaimedOrder == null) {
            this.type = OrderType.NORMAL;
        } else {
            if (reclaimedOrder.getOrderType() == OrderType.RECLAIM)
                throw new IllegalArgumentException("Eine Reklamation darf sich nicht auf eine Reklamation beziehen!");

            this.type = OrderType.RECLAIM;
        }
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

    public void add(OrderLine orderLine) {
        if (type == OrderType.RECLAIM && orderLine instanceof GSOrderLine) {
            ((GSOrderLine) orderLine).setType(OrderType.RECLAIM);
        }
        super.add(orderLine);
    }
}
