package geekshop.model;

/*
 * Created by Basti on 08.12.2014.
 */

import org.salespointframework.catalog.Product;
import org.salespointframework.inventory.InventoryItem;
import org.salespointframework.quantity.Quantity;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * An extension of {@link InventoryItem} extended by a minimal {@link Quantity} in stock which must not be undercut.
 *
 * @author Sebastian D&ouml;ring
 */

@Entity
public class GSInventoryItem extends InventoryItem {

    @OneToOne
    private Quantity minimalQuantity;

    @Deprecated
    protected GSInventoryItem() {
    }

    public GSInventoryItem(Product product, Quantity quantity, Quantity minimalQuantity) {
        super(product, quantity);
        Assert.notNull(minimalQuantity, "Minimal Quantity must be not null.");
        this.minimalQuantity = minimalQuantity;
    }

    public Quantity getMinimalQuantity() {
        return minimalQuantity;
    }

    public void setMinimalQuantity(Quantity minimalQuantity) {
        this.minimalQuantity = minimalQuantity;
    }

}
