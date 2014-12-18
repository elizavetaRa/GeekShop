package geekshop.model;

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

    private int orderNumber;

    @Enumerated(EnumType.STRING)
    private OrderType type;

    @OneToOne
    private final GSOrder reclaimedOrder;


    @Deprecated
    protected GSOrder() {
        this.orderNumber = 0;
        this.reclaimedOrder = null;
    }

    public GSOrder(int orderNumber, UserAccount ua) {
        super(ua);

        this.orderNumber = orderNumber;
        this.reclaimedOrder = null;
        initializeGSOrder(reclaimedOrder);
    }

    public GSOrder(int orderNumber, UserAccount ua, GSOrder reclaimedOrder) {
        super(ua);

        this.orderNumber = orderNumber;
        this.reclaimedOrder = reclaimedOrder;
        initializeGSOrder(reclaimedOrder);
    }

    public GSOrder(int orderNumber, UserAccount ua, PaymentMethod paymentMethod) {
        super(ua, paymentMethod);

        this.orderNumber = orderNumber;
        this.reclaimedOrder = null;
        initializeGSOrder(reclaimedOrder);
    }

    public GSOrder(int orderNumber, UserAccount ua, PaymentMethod paymentMethod, GSOrder reclaimedOrder) {
        super(ua, paymentMethod);

        this.orderNumber = orderNumber;
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


    public int getOrderNumber() {
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
