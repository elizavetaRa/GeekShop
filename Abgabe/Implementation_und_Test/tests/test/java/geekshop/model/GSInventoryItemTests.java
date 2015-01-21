package geekshop.model;

import geekshop.AbstractIntegrationTests;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.salespointframework.catalog.Product;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.quantity.Units;

/**
 * Test class for {@link GSInventoryItem}.
 *
 * @author Sebastian Döring
 */

public class GSInventoryItemTests extends AbstractIntegrationTests {

    private Product product;
    private Quantity quantity;

    @Before
    public void setUp() {
        SuperCategory superCategory = new SuperCategory("superCat");
        SubCategory subCategory = new SubCategory("subCat", superCategory);
        product = new GSProduct(100, "test1", Money.of(CurrencyUnit.EUR, 1D), subCategory);
        quantity = Units.of(5L);
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
        new GSInventoryItem(product, quantity, quantity).setMinimalQuantity(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMinimalQuantityWithNegativeArgument() {
        new GSInventoryItem(product, quantity, quantity).setMinimalQuantity(Units.of(-1));
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
