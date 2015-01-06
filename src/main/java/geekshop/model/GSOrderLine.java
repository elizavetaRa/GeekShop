package geekshop.model;

import org.salespointframework.catalog.Product;
import org.salespointframework.order.OrderLine;
import org.salespointframework.quantity.Quantity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * An extension of {@link OrderLine} extended by {@link OrderType}.
 *
 * @author Elizaveta Ragozina
 * @author Sebastian Döring
 */

@Entity
public class GSOrderLine extends OrderLine {

    @Enumerated(EnumType.STRING)
    private OrderType type;

    @Deprecated
    protected GSOrderLine() {
    }

    /**
     * Creates a new {@link GSOrderLine} with the given {@link Product} and {@link Quantity}.
     * The {@code orderType} is set to {@code NORMAL}.
     */
    public GSOrderLine(Product product, Quantity quantity) {
        super(product, quantity);

        this.type = OrderType.NORMAL;
    }


    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }
}
