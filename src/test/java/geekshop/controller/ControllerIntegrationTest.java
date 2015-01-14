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
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import javax.servlet.http.HttpSession;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    private UserAccountManager uam;
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
    @Autowired
    private PasswordRulesRepository passwordRulesRepo;


    private Model model;
    private User hans;
    private User owner;
    private GSInventoryItem testItem;
    long quantity = 1;

    @Before
    public void setup() {
        model = new ExtendedModelMap();

        hans = userRepo.findByUserAccount(uam.findByUsername("hans").get());
        owner = userRepo.findByUserAccount(uam.findByUsername("owner").get());

        login("owner", "123");
//        owner = userRepo.findByUserAccount(authManager.getCurrentUser().get());

        super.setUp();
    }

    @Test
    public void buySomething() throws Exception {
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

        cartController.addProductToCart(testProduct, quantity, query, testCart, session, Optional.of(owner.getUserAccount()), model);
        cartController.buy(testCart, session, payment, Optional.of(owner.getUserAccount()), model);

        for (GSOrder order : orderRepo.findAll()) {
            if (!order.isOpen() && !order.isCanceled()) {
                for (OrderLine ol : order.getOrderLines()) {
                    if (ol.getProductName().equals(testProduct.getName())) {

                        boughtProduct = catalog.findOne(ol.getProductIdentifier()).get();

                    }
                }
            }
        }

        assertEquals(boughtProduct, testProduct);
        assertTrue(testItem.getQuantity().getAmount().intValueExact() == 9);

    }

    @Test
    public void reclaimSomething() throws Exception {
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


        for (GSOrder order : orderRepo.findAll()) {
            if (!order.isOpen() && !order.isCanceled()) {
                for (OrderLine ol : order.getOrderLines()) {
                    if (ol.getProductName().equals(testProduct.getName())) {
                        orderNumber = order.getOrderNumber();
                    }
                }
            }
        }
        iterableCount = orderRepo.count();

        recController.allToReclaimCart(orderNumber, testCart, model, session, Optional.of(owner.getUserAccount()));
        recController.reclaimIt(testCart, String.valueOf(orderNumber), session, Optional.of(owner.getUserAccount()), model);

        assertEquals(orderRepo.count(), iterableCount + 1);
        assertEquals(currentQuantity + quantity, testItem.getQuantity().getAmount().intValueExact());

    }

    @Test
    public void accConIndexTest() throws Exception {
        mvc.perform(get("/index")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void accConAdjustPW() throws Exception {
        mvc.perform(get("/adjustpw")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        mvc.perform(post("/adjustpw")
                .with(user("hans").roles("EMPLOYEE"))
                .param("newPW", "!A2s3d4f")
                .param("retypePW", "!A2s3d4f"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void accConStaff() throws Exception {
        mvc.perform(get("/staff"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff"))
                .andExpect(model().attributeExists("staff"));
    }

    @Test
    public void accConShowEmployee() throws Exception {
        mvc.perform(get("/staff/{uai}"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("isOwnProfile"))
                .andExpect(model().attributeExists("inEditingMode"));
    }

    @Test
    public void accConHire() throws Exception {
        mvc.perform(post("/addemployee"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff"));
    }

    @Test
    public void accConFire() throws Exception {
        mvc.perform(delete("/staff/{uai}"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff"));
    }

    @Test
    public void accConFireAll() throws Exception {
        mvc.perform(delete("/firestaff"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff"));
    }

    @Test
    public void accConProfileChange1() throws Exception {
        mvc.perform(get("/staff/{uai}/{page}"))
                .andExpect(status().isOk());
//                .andExpect(view().name("welcome"))
//                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void accConChangedData() throws Exception {
        mvc.perform(post("/staff/{uai}/changedata"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff"));
    }

    @Test
    public void accConChangedPW() throws Exception {
        mvc.perform(post("/staff/{uai}/changepw"))
                .andExpect(status().isOk())
                .andExpect(view().name(""));
    }

    @Test
    public void accConSetPWRules() throws Exception {
        mvc.perform(post("/setrules"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff"));
    }

    @Test
    public void accConProfile() throws Exception {
        mvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("isOwnProfile"))
                .andExpect(model().attributeExists("inEditingMode"));
    }

    @Test
    public void accConProfileChange2() throws Exception {
        mvc.perform(get("/profile/{page}"))
                .andExpect(status().isOk())
                .andExpect(view().name(""));
    }

    @Test
    public void accConChangedOwnData() throws Exception {
        mvc.perform(post("/profile/changedata"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"));
    }

    @Test
    public void accConChangedOwnPW() throws Exception {
        mvc.perform(post("/profile/changepw"))
                .andExpect(status().isOk())
                .andExpect(view().name(""));
    }

    @Test
    public void cartConCart() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void cartConAddProductToCart() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void cartConDeleteAll() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void cartConDeleteCartItem() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void cartConUpdateCartItem() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void cartConCheckOut() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void cartConOrderOverview() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void cartConBuy() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void catConSearchEntryByName() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConOrders() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConExportXML() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConShowReclaim() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConAcceptReclaim() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConJokes() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConNewJoke() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConShowJoke() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConEditJoke() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConDeleteJoke() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConDeleteAllJokes() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConMessages() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConDeleteMessages() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConRange() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConDelSuper() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConDelSubRequest() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConDelProductRequest() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConEditProduct() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConAddProduct() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConAddProductToCatalog() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConEditSuperCategory() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConEditSuper() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConAddSuper() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConAddSuperCategory() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConEditSubCategory() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConEditSub() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConAddSubCategory() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void ownConAddSub() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void recConReclaim() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void recConReclaimCart() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void recConAddProductToReclaimCart() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void recConAllToReclaimCart() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void recConReclaimIt() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void recConReclaimBasket() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void recConSearchOrderByNumber() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void recConCheckOut() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void recConUpdateReclaimCartItem() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void recConCancelReclaim() throws Exception {
        mvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }


}
