package geekshop.model;

import org.salespointframework.order.OrderLine;
import org.springframework.util.Assert;

/**
 * Tupel that assists to bring {@link geekshop.model.GSProduct} and {@link org.salespointframework.order.OrderLine} in one Model
 *
 * @author Felix Döring
 */

public class ReclaimTupel {

    private GSProduct product;
    private OrderLine orderLine;
    private ReclaimTupel(){}

    /**
     * Creates a new {@link geekshop.model.ReclaimTupel}.
     * @param product must not be null
     * @param orderLine must not be null
     */
    public ReclaimTupel (GSProduct product, OrderLine orderLine){
        Assert.notNull(product, "Product must not be null");
        Assert.notNull(orderLine, "OrderLine must not be null");

        this.product = product;
        this.orderLine = orderLine;
    }

    public GSProduct getProduct() {
        return product;
    }

    public OrderLine getOrderLine() {
        return orderLine;
    }
}
