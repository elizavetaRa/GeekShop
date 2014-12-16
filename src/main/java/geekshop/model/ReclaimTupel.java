package geekshop.model;

import org.salespointframework.order.OrderLine;
import org.springframework.util.Assert;

/**
 * Created by h4llow3En on 15/12/14.
 */
public class ReclaimTupel {

    private GSProduct product;
    private OrderLine orderLine;
    private ReclaimTupel(){}

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
