package geekshop.model;

/*
 * Created by Lisa on 30.11.2014.
 */

import org.salespointframework.catalog.Product;
import org.salespointframework.order.OrderLine;
import org.salespointframework.quantity.Quantity;

import javax.persistence.Entity;
import java.math.BigDecimal;

/**
 * An extension of {@link OrderLine} extended by {@link OrderLineState}.
 *
 * @author Elizaveta Ragozina
 * @author Sebastian D&ouml;ring
 */

@Entity
public class GSOrderLine extends OrderLine {

//    @Enumerated(EnumType.STRING)
//    private OrderLineState state;
//    @Lob
//    private Quantity reclaimedQuantity;
    private BigDecimal reclaimedAmount;


    @Deprecated
    protected GSOrderLine() {
    }

    public GSOrderLine(Product product, Quantity quantity/*, Quantity reclaimedQuantity*/) {
        super(product, quantity);
//        Assert.notNull(state, "OrderLineState must not be null.");
//        this.state = state;
//        this.reclaimedQuantity = new Quantity(BigDecimal.ZERO, quantity.getMetric(), quantity.getRoundingStrategy());
        this.reclaimedAmount = BigDecimal.ZERO;
    }

//    public OrderLineState getOrderLineState() {
//        return state;
//    }

//    public void setOrderLineState(OrderLineState state) {            /*Im EKD in GSOrder??*/
//        this.state = state;
//    }

//    public Quantity getReclaimedQuantity() {
//        return reclaimedQuantity;
//    }
//
//    public void increaseReclaimedQuantity(BigDecimal amount) {
//        reclaimedQuantity = reclaimedQuantity.add(new Quantity(amount, getQuantity().getMetric(), getQuantity().getRoundingStrategy()));
//    }

    public BigDecimal getReclaimedAmount() {
        return reclaimedAmount;
    }

    public void increaseReclaimedAmount(BigDecimal amount) {
        reclaimedAmount = reclaimedAmount.add(amount);
    }
}
