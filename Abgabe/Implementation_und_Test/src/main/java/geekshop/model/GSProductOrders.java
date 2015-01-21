package geekshop.model;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.quantity.Units;

import java.math.BigDecimal;
import java.util.TreeSet;

/**
 * A helper class for view to encapsulate all {@link geekshop.model.GSOrderLine}s
 * related to a specific {@link org.salespointframework.catalog.Product} as well as the total price and total quantity.
 *
 * @author Sebastian Döring
 */

public class GSProductOrders {

    private Money totalPrice;
    private Quantity totalQuantity;
    private TreeSet<GSProductOrder> productOrders;


    /**
     * Creates new {@link GSProductOrders}.
     */
    public GSProductOrders() {
        this.totalPrice = Money.zero(CurrencyUnit.EUR);
        this.totalQuantity = Units.ZERO;
        this.productOrders = new TreeSet<GSProductOrder>();
    }


    public Money getTotalPrice() {
        return totalPrice;
    }

    private void setTotalPrice(Money totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Quantity getTotalQuantity() {
        return totalQuantity;
    }

    private void setTotalQuantity(Quantity totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public TreeSet<GSProductOrder> getProductOrders() {
        return productOrders;
    }

    /**
     * Adds a {@link GSProductOrder} to the set and updates total price and total quantity correspondly.
     */
    public void addProductOrder(GSProductOrder productOrder) {
        productOrders.add(productOrder);
        GSOrderLine ol = productOrder.getOrderLine();

        BigDecimal amount = ol.getQuantity().getAmount();
        if (ol.getType() == OrderType.RECLAIM)
            amount = amount.multiply(BigDecimal.valueOf(-1));

        totalQuantity = new Quantity(totalQuantity.getAmount().add(amount),
                ol.getQuantity().getMetric(), ol.getQuantity().getRoundingStrategy());

        Money price = ol.getPrice();
        if (ol.getType() == OrderType.RECLAIM)
            price = price.multipliedBy(-1);

        totalPrice = Money.total(totalPrice, price);
    }
}
