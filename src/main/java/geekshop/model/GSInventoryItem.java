package geekshop.model;

import org.salespointframework.catalog.Product;
import org.salespointframework.inventory.InventoryItem;
import org.salespointframework.quantity.Quantity;
import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

/**
 * An extension of {@link InventoryItem} extended by a minimal {@link Quantity} in stock which must not be undercut.
 *
 * @author Sebastian Döring
 */

@Entity
public class GSInventoryItem extends InventoryItem {

    @Lob
    @Column(length = 4096)
    private Quantity minimalQuantity;

    @Deprecated
    protected GSInventoryItem() {
    }

    /**
     * Creates a new {@link GSInventoryItem} for the given product and quantity. Furthermore a minimal {@link Quantity} is required which must not be undercut in stock.
     *
     * @param minimalQuantity must not be {@literal null} or negative.
     */
    public GSInventoryItem(Product product, Quantity quantity, Quantity minimalQuantity) {
        super(product, quantity);
        Assert.notNull(minimalQuantity, "Minimal quantity must be not null.");
        Assert.isTrue(!minimalQuantity.isNegative(), "Minimal quantity must have positive amount.");
        this.minimalQuantity = minimalQuantity;
    }

    public Quantity getMinimalQuantity() {
        return minimalQuantity;
    }

    /**
     * @param minimalQuantity must not be {@literal null} or negative.
     */
    public void setMinimalQuantity(Quantity minimalQuantity) {
        Assert.notNull(minimalQuantity, "Minimal quantity must be not null.");
        Assert.isTrue(!minimalQuantity.isNegative(), "Minimal quantity must have positive amount.");
        this.minimalQuantity = minimalQuantity;
    }

    /**
     * Returns whether the {@link InventoryItem} is available in at least the minimal quantity.
     */
    public boolean hasSufficientQuantity() {
        return !getQuantity().subtract(this.minimalQuantity).isNegative();
    }

}
