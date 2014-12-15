package geekshop.model;

/*
 * Created by Basti on 08.12.2014.
 */

import org.joda.money.Money;
import org.salespointframework.quantity.Quantity;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * A helper class to encapsulate all {@link geekshop.model.GSOrder}s related to a specific {@link org.salespointframework.catalog.Product} as well as the total price and total quantity.
 *
 * @author Sebastian D&ouml;ring
 */

public class GSProductOrders {

    private Money totalPrice;
    private Quantity totalQuantity;
    private List<GSProductOrder> productOrders;


    public GSProductOrders() {
        this.productOrders = new LinkedList<GSProductOrder>();
    }

    public Money getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Money totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Quantity getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Quantity totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public List<GSProductOrder> getProductOrders() {
        return productOrders;
    }

    public void addProductOrder(GSProductOrder productOrder) {
        productOrders.add(productOrder);
        GSOrderLine ol = productOrder.getOrderLine();

        BigDecimal amount = ol.getQuantity().getAmount();
        if (ol.getType() == OrderType.RECLAIM)
            amount = amount.multiply(BigDecimal.valueOf(-1));

        totalQuantity = new Quantity(
                totalQuantity == null ? amount : totalQuantity.getAmount().add(amount),
                ol.getQuantity().getMetric(), ol.getQuantity().getRoundingStrategy());

        Money price = ol.getPrice();
        if (ol.getType() == OrderType.RECLAIM)
            price = price.multipliedBy(-1);

        if (totalPrice == null) {
            totalPrice = price;
        } else {
            totalPrice = Money.total(totalPrice, price);
        }
    }
}
