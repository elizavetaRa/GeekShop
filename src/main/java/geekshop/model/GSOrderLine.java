package geekshop.model;

/*
 * Created by Lisa on 30.11.2014.
 */

import org.salespointframework.catalog.Product;
import org.salespointframework.order.OrderLine;
import org.salespointframework.quantity.Quantity;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * An extension of {@link OrderLine} extended by {@link OrderLineState}.
 *
 * @author Elizaveta Ragozina
 * @author Sebastian D&ouml;ring
 */

@Entity
public class GSOrderLine extends OrderLine {

    @Enumerated(EnumType.STRING)
    private OrderLineState state;


    @Deprecated
    protected GSOrderLine() {
    }

    public GSOrderLine(Product product, Quantity quantity, OrderLineState state) {
        super(product, quantity);
        Assert.notNull(state, "OrderLineState must not be null.");
        this.state = state;
    }

    public OrderLineState getOrderLineState() {
        return state;
    }

    public void setOrderLineState(OrderLineState state) {            /*Im EKD in GSOrder??*/
        this.state = state;
    }
}
