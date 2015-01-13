package geekshop.controller;

import geekshop.AbstractWebIntegrationTests;
import geekshop.model.*;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.order.Cart;
import org.salespointframework.order.OrderLine;
import org.salespointframework.quantity.Units;
import org.salespointframework.useraccount.AuthenticationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import javax.servlet.http.HttpSession;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Midokin on 12.01.2015.
 */

@WebAppConfiguration
public class ControllerIntegrationTest extends AbstractWebIntegrationTests {

    @Autowired
    private AccountController accController;
    @Autowired
    private CartController cartController;
    @Autowired
    private CatalogController catController;
    @Autowired
    private OwnerController ownController;
    @Autowired
    private ReclaimController recController;
    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private HttpSession session;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private GSOrderRepository orderRepo;
    @Autowired
    private Catalog<GSProduct> catalog;
    @Autowired
    private Inventory<GSInventoryItem> inventory;
    @Autowired
    private SubCategoryRepository subCatRepo;
    @Autowired
    private SuperCategoryRepository supCatRepo;

    private Model model;
    private User user;
    private GSInventoryItem testItem;
    long quantity = 1;

    @Before
    public void setUp() {
        model = new ExtendedModelMap();

        login("owner", "123");
        user = userRepo.findByUserAccount(authManager.getCurrentUser().get());

    }

    @Test
    public void buySomething() throws Exception{
        Money price = Money.parse("EUR 5.00");
        Cart testCart = new Cart();
        String query = "TestProduct=1";
        String payment = "CASH";
        long productNumber = 101;
        SuperCategory testSupCat = new SuperCategory("TestSupCat");
        supCatRepo.save(testSupCat);
        SubCategory testSubCat = new SubCategory("TestSubCat", testSupCat);
        subCatRepo.save(testSubCat);
        GSProduct testProduct = new GSProduct(productNumber, "TestProduct", price, testSubCat);
        testItem = new GSInventoryItem(testProduct, Units.TEN, Units.of(5L));
        inventory.save(testItem);
        GSProduct boughtProduct = new GSProduct(0, "WrongTestProduct", Money.parse("EUR 2.00"), testSubCat);

        assertNotNull(supCatRepo.findByName("TestSupCat"));
        assertNotNull(subCatRepo.findByName("TestSubCat"));
        assertNotNull(inventory.findByProduct(testProduct));

        cartController.addProductToCart(testProduct, quantity, query, testCart, session, Optional.of(user.getUserAccount()), model);
        cartController.buy(testCart, session, payment, Optional.of(user.getUserAccount()), model);

        for (GSOrder order : orderRepo.findAll()){
            if (!order.isOpen() && !order.isCanceled()){
                for (OrderLine ol : order.getOrderLines()){
                    if (ol.getProductName().equals(testProduct.getName())){

                        boughtProduct = catalog.findOne(ol.getProductIdentifier()).get();

                    }
                }
            }
        }

        assertEquals(boughtProduct, testProduct);
        assertTrue(testItem.getQuantity().getAmount().intValueExact() == 9);

    }

    @Test
    public void reclaimSomething() throws Exception{
        Money price = Money.parse("EUR 5.00");
        Cart testCart = new Cart();
        long productNumber = 101;
        long currentQuantity = testItem.getQuantity().getAmount().intValueExact();
        long orderNumber = 0;
        long iterableCount;
        SuperCategory testSupCat = new SuperCategory("TestSupCat");
        supCatRepo.save(testSupCat);
        SubCategory testSubCat = new SubCategory("TestSubCat", testSupCat);
        subCatRepo.save(testSubCat);
        GSProduct testProduct = new GSProduct(productNumber, "TestProduct", price, testSubCat);


        for (GSOrder order : orderRepo.findAll()){
            if (!order.isOpen() && !order.isCanceled()){
                for (OrderLine ol : order.getOrderLines()){
                    if (ol.getProductName().equals(testProduct.getName())){
                        orderNumber = order.getOrderNumber();
                    }
                }
            }
        }
        iterableCount = orderRepo.count();

        recController.allToReclaimCart(orderNumber, testCart, model, session, Optional.of(user.getUserAccount()));
        recController.reclaimIt(testCart, String.valueOf(orderNumber), session, Optional.of(user.getUserAccount()), model);

        assertEquals(orderRepo.count(), iterableCount + 1);
        assertEquals(currentQuantity + quantity, testItem.getQuantity().getAmount().intValueExact());

    }




}
