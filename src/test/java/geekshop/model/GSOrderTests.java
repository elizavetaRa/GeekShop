package geekshop.model;

import geekshop.AbstractIntegrationTests;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.salespointframework.catalog.Product;
import org.salespointframework.catalog.ProductIdentifier;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.order.OrderLine;
import org.salespointframework.order.OrderStatus;
import org.salespointframework.payment.Cash;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.quantity.Units;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.fail;

/**
 * Test class for {@link GSOrder}.
 *
 * @author Sebastian Döring
 */

public class GSOrderTests extends AbstractIntegrationTests {

    @Autowired
    private GSOrderRepository orderRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private Inventory<GSInventoryItem> inventory;
    @Autowired
    private SubCategoryRepository subCatRepo;
    @Autowired
    private MessageRepository messageRepo;
    @Autowired
    private BusinessTime businessTime;

    private UserAccount ua;
    private GSOrder openNormalOrder;
    private GSOrder paidNormalOrder;
    private GSOrder openReclaimOrder;
    private GSInventoryItem item1;
    private GSInventoryItem item2;
    private GSInventoryItem item3;
    private GSInventoryItem item4;
    private GSInventoryItem item5;
    private GSInventoryItem item6;

    @Before
    public void setUp() {
        ua = userRepo.findAll().iterator().next().getUserAccount();

        SubCategory subCategory = subCatRepo.findAll().iterator().next();
        GSProduct prod1 = new GSProduct(100, "test1", Money.of(CurrencyUnit.EUR, 1D), subCategory);
        GSProduct prod2 = new GSProduct(101, "test2", Money.of(CurrencyUnit.EUR, 2D), subCategory);
        GSProduct prod3 = new GSProduct(102, "test3", Money.of(CurrencyUnit.EUR, 3D), subCategory);
        GSProduct prod4 = new GSProduct(103, "test4", Money.of(CurrencyUnit.EUR, 4D), subCategory);
        GSProduct prod5 = new GSProduct(104, "test5", Money.of(CurrencyUnit.EUR, 5D), subCategory);
        GSProduct prod6 = new GSProduct(105, "test6", Money.of(CurrencyUnit.EUR, 6D), subCategory);
        item1 = new GSInventoryItem(prod1, Units.TEN, Units.of(5L));
        item2 = new GSInventoryItem(prod2, Units.TEN, Units.of(5L));
        item3 = new GSInventoryItem(prod3, Units.TEN, Units.of(5L));
        item4 = new GSInventoryItem(prod4, Units.TEN, Units.of(5L));
        item5 = new GSInventoryItem(prod5, Units.TEN, Units.of(5L));
        item6 = new GSInventoryItem(prod6, Units.TEN, Units.of(5L));
        inventory.save(item1);
        inventory.save(item2);
        inventory.save(item3);
        inventory.save(item4);
        inventory.save(item5);
        inventory.save(item6);

        openNormalOrder = new GSOrder(ua, Cash.CASH);
        openNormalOrder.add(new GSOrderLine(prod1, Units.ONE));
        openNormalOrder.add(new GSOrderLine(prod2, Units.of(5L)));
        openNormalOrder.add(new GSOrderLine(prod3, Units.of(6L)));

        paidNormalOrder = new GSOrder(ua, Cash.CASH);
        paidNormalOrder.add(new GSOrderLine(prod4, Units.ONE));
        paidNormalOrder.add(new GSOrderLine(prod5, Units.of(5L)));
        paidNormalOrder.add(new GSOrderLine(prod6, Units.of(6L)));
        paidNormalOrder.pay();

        openReclaimOrder = new GSOrder(ua, Cash.CASH, paidNormalOrder);
        openReclaimOrder.add(new GSOrderLine(prod4, Units.ONE));
        openReclaimOrder.add(new GSOrderLine(prod5, Units.of(2L)));
        openReclaimOrder.add(new GSOrderLine(prod6, Units.of(3L)));
    }

    @Test
    public void testConstructor() {
        try {
            new GSOrder(null);
            fail("There should be thrown an IllegalArgumentException if UserAccount is null!");
        } catch (IllegalArgumentException ignored) {
        }

        Assert.assertEquals("PaymentMethod should be CASH!", Cash.CASH, openNormalOrder.getPaymentMethod());
        Assert.assertEquals("Order should be OPEN!", OrderStatus.OPEN, openNormalOrder.getOrderStatus());
        Assert.assertEquals("Order should be a normal order!", OrderType.NORMAL, openNormalOrder.getOrderType());

        Assert.assertEquals("Order should be a reclaim!", OrderType.RECLAIM, openReclaimOrder.getOrderType());
        Assert.assertEquals("Reclaimed order is not the expected one!", paidNormalOrder, openReclaimOrder.getReclaimedOrder());
        openReclaimOrder.pay();
        try {
            new GSOrder(ua, openReclaimOrder);
            fail("There should be thrown an IllegalArgumentException if a reclaim relates to a reclaim!");
        } catch (IllegalArgumentException ignored) {
        }
        try {
            new GSOrder(ua, openNormalOrder);
            fail("There should be thrown an IllegalArgumentException if a reclaim relates to an open order!");
        } catch (IllegalArgumentException ignored) {
        }
        paidNormalOrder.complete();
        try {
            new GSOrder(ua, paidNormalOrder);
            fail("There should be thrown an IllegalArgumentException if a reclaim relates to a completed order!");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testOrderNumbers() {
        Set<Long> orderNumbers = new HashSet<>();
        for (GSOrder order : orderRepo.findAll()) {
            if (!orderNumbers.add(order.getOrderNumber())) {
                fail("The allocated order numbers are not unique!");
            }
        }

        for (int i = 0; i < 100; i++) {
            if (!orderNumbers.add(new GSOrder(ua).getOrderNumber())) {
                fail("The allocated order numbers are not unique!");
            }
        }
    }

    //region testAdd
    @Test(expected = IllegalArgumentException.class)
    public void testAddWithIllegalOrderLineProduct() {
        GSOrderLine ol = new GSOrderLine(new Product("test", Money.of(CurrencyUnit.EUR, 1L), Units.METRIC), Units.ONE);
        new GSOrder(ua, paidNormalOrder).add(ol);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddWithIllegalOrderLineQuantity() {
        GSOrderLine ol = new GSOrderLine(item4.getProduct(), Units.TEN);
        new GSOrder(ua, paidNormalOrder).add(ol);
    }

    @Test
    public void testAddOrderLineState() {
        GSOrderLine ol = new GSOrderLine(item4.getProduct(), Units.ONE);
        new GSOrder(ua, paidNormalOrder).add(ol);
        Assert.assertEquals("Order lines of a reclaim order must be marked as reclaim order lines!", OrderType.RECLAIM, ol.getType());
    }
    //endregion

    //region testPay
    @Test(expected = IllegalStateException.class)
    public void testPayWithIllegalOrderState() {
        paidNormalOrder.pay();
    }

    @Test(expected = IllegalStateException.class)
    public void testPayWithNullPaymentMethod() {
        new GSOrder(ua).pay();
    }

    @Test(expected = IllegalStateException.class)
    public void testPayWithoutOrderLines() {
        new GSOrder(ua, Cash.CASH).pay();
    }

    @Test
    public void testPayStatusAndDate() {
        openNormalOrder.pay();

        Assert.assertEquals("Order has not the status PAID!", OrderStatus.PAID, openNormalOrder.getOrderStatus());
        Assert.assertNotNull("Date created of order is still null!", openNormalOrder.getDateCreated());
    }

    @Test
    public void testPayInventoryUpdateNormalOrder() {
        Map<ProductIdentifier, Quantity> stockQuants = new HashMap<>();
        stockQuants.put(item1.getProduct().getId(), item1.getQuantity());
        stockQuants.put(item2.getProduct().getId(), item2.getQuantity());
        stockQuants.put(item3.getProduct().getId(), item3.getQuantity());
        long cntMessages = messageRepo.count();

        openNormalOrder.pay();

        for (OrderLine ol : openNormalOrder.getOrderLines()) {
            GSInventoryItem item = inventory.findByProductIdentifier(ol.getProductIdentifier()).get();
            Quantity newQuantity = item.getQuantity();
            Assert.assertEquals("After pay(), Inventory has not been updated correctly!",
                    stockQuants.get(ol.getProductIdentifier()).subtract(ol.getQuantity()), newQuantity);

            if (newQuantity.subtract(item.getMinimalQuantity()).isNegative()) {
                Assert.assertEquals("There should be sent a message to the owner if the actual quantity is falling below the minimal quantity!",
                        cntMessages + 1, messageRepo.count());
            }
        }
    }

    @Test
    public void testPayInventoryUpdateReclaimOrder() {
        Map<ProductIdentifier, Quantity> stockQuants = new HashMap<>();
        stockQuants.put(item4.getProduct().getId(), item4.getQuantity());
        stockQuants.put(item5.getProduct().getId(), item5.getQuantity());
        stockQuants.put(item6.getProduct().getId(), item6.getQuantity());

        openReclaimOrder.pay();

        for (OrderLine ol : openReclaimOrder.getOrderLines()) {
            GSInventoryItem item = inventory.findByProductIdentifier(ol.getProductIdentifier()).get();
            Quantity newQuantity = item.getQuantity();
            Assert.assertEquals("After pay(), Inventory has not been updated correctly!",
                    stockQuants.get(ol.getProductIdentifier()).add(ol.getQuantity()), newQuantity);
        }
    }
    //endregion

    //region testComplete
    @Test(expected = IllegalStateException.class)
    public void testCompleteOpen() {
        openNormalOrder.complete();
    }

    @Test(expected = IllegalStateException.class)
    public void testCompleteCancelled() {
        openNormalOrder.cancel();
        openNormalOrder.complete();
    }

    @Test(expected = IllegalStateException.class)
    public void testCompleteCompleted() {
        paidNormalOrder.complete();
        paidNormalOrder.complete();
    }

    @Test
    public void testCompleteStatus() {
        paidNormalOrder.complete();
        Assert.assertEquals("Completed order should have status COMPLETED!", OrderStatus.COMPLETED, paidNormalOrder.getOrderStatus());
    }
    //endregion

    //region testCancel
    @Test(expected = IllegalStateException.class)
    public void testCancelPaid() {
        paidNormalOrder.cancel();
    }

    @Test(expected = IllegalStateException.class)
    public void testCancelCompleted() {
        paidNormalOrder.complete();
        paidNormalOrder.cancel();
    }

    @Test(expected = IllegalStateException.class)
    public void testCancelCancelled() {
        openNormalOrder.cancel();
        openNormalOrder.cancel();
    }

    @Test
    public void testCancelStatus() {
        openNormalOrder.cancel();
        Assert.assertEquals("Cancelled order should have status CANCELLED!", OrderStatus.CANCELLED, openNormalOrder.getOrderStatus());
    }
    //endregion

    //region testIs[Status]
    @Test
    public void testIsOpen() {
        Assert.assertTrue("isOpen() should return true for open order!", openNormalOrder.isOpen());

        openNormalOrder.cancel();
        Assert.assertFalse("isOpen() should return false for cancelled order!", openNormalOrder.isOpen());
        Assert.assertFalse("isOpen() should return false for paid order!", paidNormalOrder.isOpen());
        paidNormalOrder.complete();
        Assert.assertFalse("isOpen() should return false for completed order!", paidNormalOrder.isOpen());
    }

    @Test
    public void testIsPaid() {
        Assert.assertTrue("isPaid() should return true for paid order!", paidNormalOrder.isPaid());
        paidNormalOrder.complete();
        Assert.assertTrue("isPaid() should return true for completed order!", paidNormalOrder.isPaid());

        Assert.assertFalse("isPaid() should return false for open order!", openNormalOrder.isPaid());
        openNormalOrder.cancel();
        Assert.assertFalse("isPaid() should return false for cancelled order!", openNormalOrder.isPaid());
    }

    @Test
    public void testIsCompleted() {
        paidNormalOrder.complete();
        Assert.assertTrue("isCompleted() should return true for completed order!", paidNormalOrder.isCompleted());

        Assert.assertFalse("isCompleted() should return false for open order!", openNormalOrder.isCompleted());
        openNormalOrder.cancel();
        Assert.assertFalse("isCompleted() should return false for cancelled order!", openNormalOrder.isCompleted());
    }

    @Test
    public void testIsCompleted14Days() {
        Assert.assertFalse("isCompleted() should return false for paid order after less than 14 days!", paidNormalOrder.isCompleted());
        businessTime.forward(Duration.ofDays(14L));
        Assert.assertFalse("isCompleted() should return false for paid order after exactly 14 days!", paidNormalOrder.isCompleted());
        businessTime.forward(Duration.ofDays(1L));
        Assert.assertTrue("isCompleted() should return true for paid order after more than 14 days!", paidNormalOrder.isCompleted());
    }

    @Test
    public void testIsCanceled() {
        Assert.assertFalse("isCanceled() should return false for open order!", openNormalOrder.isCanceled());

        openNormalOrder.cancel();
        Assert.assertTrue("isCanceled() should return true for canceled order!", openNormalOrder.isCanceled());

        Assert.assertFalse("isCanceled() should return false for paid order!", paidNormalOrder.isCanceled());
        paidNormalOrder.complete();
        Assert.assertFalse("isCanceled() should return false for completed order!", paidNormalOrder.isCanceled());
    }
    //endregion

    //region testFindOrderLineByProduct
    @Test
    public void testFindOrderLineByProduct() {
        for (OrderLine ol : openNormalOrder.getOrderLines()) {
            Assert.assertEquals("findOrderLineByProduct returns wrong order line!", ol,
                    openNormalOrder.findOrderLineByProduct(inventory.findByProductIdentifier(ol.getProductIdentifier()).get().getProduct()));
            Assert.assertEquals("findOrderLineByProductIdentifier returns wrong order line!", ol,
                    openNormalOrder.findOrderLineByProductIdentifier(ol.getProductIdentifier()));
        }

        Assert.assertNull("findOrderLineByProduct should return null if there is no order line belonging to the given product!",
                openNormalOrder.findOrderLineByProduct(item4.getProduct()));
        Assert.assertNull("findOrderLineByProductIdentifier should return null if there is no order line belonging to the given product!",
                openNormalOrder.findOrderLineByProductIdentifier(item4.getProduct().getId()));
    }
    //endregion

    //region testLongToString
    @Test
    public void testLongToString() {
        Assert.assertEquals("longToString does not return string representing the original order number properly!",
                openNormalOrder.getOrderNumber(), Long.parseLong(GSOrder.longToString(openNormalOrder.getOrderNumber())));
    }
    //endregion

    //region testCompareTo
    @Test
    public void testCompareTo() {
        Assert.assertEquals("openNormalOrder.compareTo(openReclaimOrder) should return -1!", -1, openNormalOrder.compareTo(openReclaimOrder));
        Assert.assertEquals("openNormalOrder.compareTo(openNormalOrder) should return 0!", 0, openNormalOrder.compareTo(openNormalOrder));
        Assert.assertEquals("openReclaimOrder.compareTo(openNormalOrder) should return 1!", 1, openReclaimOrder.compareTo(openNormalOrder));
    }
    //endregion
}
