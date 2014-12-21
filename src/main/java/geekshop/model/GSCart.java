package geekshop.model;

import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;


/**
 * Created by Lisa on 21.12.2014.
 */
public class GSCart extends Cart implements Iterable<CartItem> {

    private boolean reclaimModus;

    public GSCart() {
        super();
        this.reclaimModus = true;

    }

    public boolean isReclaimModus() {
        return reclaimModus;
    }

    public void switchModus() {

        if (this.reclaimModus) {
            this.reclaimModus = false;
        } else
            this.reclaimModus = true;

    }


}


