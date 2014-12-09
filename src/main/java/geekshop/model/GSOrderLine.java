package geekshop.model;

/*
 * Created by Lisa on 30.11.2014.
 */

import org.salespointframework.catalog.Product;
import org.salespointframework.order.OrderLine;
import org.salespointframework.quantity.Quantity;

import javax.persistence.Entity;
import javax.persistence.Lob;
import java.math.BigDecimal;

/**
 * An extension of {@link OrderLine} extended by {@link OrderLineState}.
 *
 * @author Elizaveta Ragozina
 * @author Sebastian D&ouml;ring
 */

@Entity
public class GSOrderLine extends OrderLine {

    @Lob
    private BigDecimal reclaimedAmount;


    @Deprecated
    protected GSOrderLine() {
    }

    public GSOrderLine(Product product, Quantity quantity/*, Quantity reclaimedQuantity*/) {
        super(product, quantity);

        this.reclaimedAmount = BigDecimal.ZERO;
    }


    public BigDecimal getReclaimedAmount() {
        return reclaimedAmount;
    }

    public void increaseReclaimedAmount(BigDecimal amount) {
        reclaimedAmount = reclaimedAmount.add(amount);
    }
}
