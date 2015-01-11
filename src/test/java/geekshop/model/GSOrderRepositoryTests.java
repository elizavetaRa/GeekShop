package geekshop.model;

import geekshop.AbstractIntegrationTests;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.payment.Cash;
import org.salespointframework.quantity.Units;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

/**
 * Test class for {@link GSOrderRepository}.
 *
 * @author Sebastian Döring
 */

public class GSOrderRepositoryTests extends AbstractIntegrationTests {

    @Autowired
    private GSOrderRepository orderRepo;
    @Autowired
    private UserAccountManager uam;
    @Autowired
    private Inventory<GSInventoryItem> inventory;
    @Autowired
    private SubCategoryRepository subCatRepo;
    @Autowired
    private SuperCategoryRepository superCatRepo;

    private UserAccount ua;
    private GSOrder paidOrder;
    private GSOrder normal1;
    private GSOrder normal2;
    private GSOrder reclaim1;
    private GSOrder reclaim2;

    @Before
    public void setUp() {
        ua = uam.create("test", "test");
        uam.save(ua);

        SuperCategory superCategory = new SuperCategory("superCat");
        superCatRepo.save(superCategory);
        SubCategory subCategory = new SubCategory("subCat", superCategory);
        subCatRepo.save(subCategory);
        GSProduct prod1 = new GSProduct(100, "test1", Money.of(CurrencyUnit.EUR, 1D), subCategory);
        GSProduct prod2 = new GSProduct(101, "test2", Money.of(CurrencyUnit.EUR, 2D), subCategory);
        GSProduct prod3 = new GSProduct(102, "test3", Money.of(CurrencyUnit.EUR, 3D), subCategory);
        GSInventoryItem item1 = new GSInventoryItem(prod1, Units.TEN, Units.of(5L));
        GSInventoryItem item2 = new GSInventoryItem(prod2, Units.TEN, Units.of(5L));
        GSInventoryItem item3 = new GSInventoryItem(prod3, Units.TEN, Units.of(5L));
        inventory.save(item1);
        inventory.save(item2);
        inventory.save(item3);

        paidOrder = new GSOrder(ua, Cash.CASH);
        paidOrder.add(new GSOrderLine(prod1, Units.ONE));
        paidOrder.add(new GSOrderLine(prod2, Units.ONE));
        paidOrder.add(new GSOrderLine(prod3, Units.ONE));
        paidOrder.pay();

        normal1 = new GSOrder(ua);
        normal2 = new GSOrder(ua);
        reclaim1 = new GSOrder(ua, paidOrder);
        reclaim2 = new GSOrder(ua, paidOrder);

        orderRepo.save(Arrays.asList(paidOrder, normal1, normal2, reclaim1, reclaim2));
    }

    @Test
    public void testFindByOrderNumber() {
        GSOrder unsavedOrder = new GSOrder(ua);
        Assert.assertEquals("GSOrderRepository does not find correct order by order number!", normal1, orderRepo.findByOrderNumber(normal1.getOrderNumber()).get());
        Assert.assertEquals("GSOrderRepository does not find correct order by order number!", normal2, orderRepo.findByOrderNumber(normal2.getOrderNumber()).get());
        Assert.assertEquals("GSOrderRepository does not find correct order by order number!", reclaim1, orderRepo.findByOrderNumber(reclaim1.getOrderNumber()).get());
        Assert.assertFalse("GSOrderRepository should not find any order which was not stored!", orderRepo.findByOrderNumber(unsavedOrder.getOrderNumber()).isPresent());
    }

    @Test
    public void testFindByType() {
        for (GSOrder o : orderRepo.findByType(OrderType.NORMAL)) {
            Assert.assertEquals("GSOrderRepository does not find correct orders by order type!", OrderType.NORMAL, o.getOrderType());
        }

        for (GSOrder o : orderRepo.findByType(OrderType.RECLAIM)) {
            Assert.assertEquals("GSOrderRepository does not find correct orders by order type!", OrderType.RECLAIM, o.getOrderType());
        }
    }

    @Test
    public void testFindByReclaimedOrder() {
        for (GSOrder o : orderRepo.findByReclaimedOrder(paidOrder)) {
            Assert.assertEquals("GSOrderRepository does not find correct orders by reclaimed order!", paidOrder, o.getReclaimedOrder());
        }
    }
}
