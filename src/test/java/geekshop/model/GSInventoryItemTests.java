package geekshop.model;

import geekshop.AbstractIntegrationTests;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.salespointframework.catalog.Product;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.quantity.Units;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test class for {@link GSInventoryItem}.
 *
 * @author Sebastian Döring
 */

public class GSInventoryItemTests extends AbstractIntegrationTests {

    @Autowired
    private Inventory<GSInventoryItem> inventory;
    private GSInventoryItem item;
    private Product product;
    private Quantity quantity;

    @Before
    public void setUp() {
        item = inventory.findAll().iterator().next();
        product = item.getProduct();
        quantity = item.getQuantity();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullMinimalQuantity() {
        new GSInventoryItem(product, quantity, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativeMinimalQuantity() {
        new GSInventoryItem(product, quantity, Units.of(-1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMinimalQuantityWithNullArgument() {
        item.setMinimalQuantity(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMinimalQuantityWithNegativeArgument() {
        item.setMinimalQuantity(Units.of(-1));
    }

    @Test
    public void testHasSufficientQuantity() {
        Quantity actQuantity = Units.ONE;
        Quantity minQuantity1 = Units.ZERO;
        Quantity minQuantity2 = Units.ONE;
        Quantity minQuantity3 = Units.of(2L);
        GSInventoryItem[] items = {
                new GSInventoryItem(product, actQuantity, minQuantity1),
                new GSInventoryItem(product, actQuantity, minQuantity2),
                new GSInventoryItem(product, actQuantity, minQuantity3),
        };

        for (GSInventoryItem item : items) {
            boolean sufficient = item.getQuantity().isGreaterThanOrEqualTo(item.getMinimalQuantity());

            Assert.assertSame("hasSufficientQuantity() returns for quantity " + item.getQuantity()
                            + " and minimal quantity " + item.getMinimalQuantity() + " " + item.hasSufficientQuantity() + "!",
                    item.hasSufficientQuantity(), sufficient);
        }
    }

}
