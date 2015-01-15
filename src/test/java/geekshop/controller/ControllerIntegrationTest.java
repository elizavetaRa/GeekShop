package geekshop.controller;

import geekshop.AbstractWebIntegrationTests;
import geekshop.model.*;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.ProductIdentifier;
import org.salespointframework.core.SalespointIdentifier;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.order.OrderLine;
import org.salespointframework.quantity.Units;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountIdentifier;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.Map;

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
    private MessageRepository messageRepo;
    @Autowired
    private JokeRepository jokeRepo;


    private Model model;
    private User hans;
    private User owner;
    private GSInventoryItem testItem;

    @Before
    public void setup() {
        model = new ExtendedModelMap();

        hans = userRepo.findByUserAccount(uam.findByUsername("hans").get());
        owner = userRepo.findByUserAccount(uam.findByUsername("owner").get());

        login("owner", "123");

        super.setUp();
    }

    @Test
    public void buySomething() throws Exception {
        Money price = Money.parse("EUR 5.00");
        String query = "q=aufkl&sort=name&cat=";
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

        mvc.perform(post("/cart")
                .with(user("owner").roles("OWNER"))
                .param("pid", testProduct.toString())
                .param("number", "1")
                .param("query", query))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/productsearch"))
                .andExpect(model().attributeExists("q"))
                .andExpect(model().attributeExists("sort"))
                .andExpect(model().attributeExists("cat"));

        mvc.perform((post("/buy")
                .with(user("owner").roles("OWNER"))
                .param("payment", payment)))
                .andExpect(status().isOk())
                .andExpect(view().name("orderoverview"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("orderoverview"));

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
        long productNumber = 101;
        long orderNumber = 0;
        long iterableCount;
        SuperCategory testSupCat = new SuperCategory("TestSupCat");
        supCatRepo.save(testSupCat);
        SubCategory testSubCat = new SubCategory("TestSubCat", testSupCat);
        subCatRepo.save(testSubCat);
        GSProduct testProduct = new GSProduct(productNumber, "TestProduct", price, testSubCat);
        testItem = new GSInventoryItem(testProduct, Units.TEN, Units.of(5L));
        inventory.save(testItem);
        long currentQuantity = testItem.getQuantity().getAmount().longValue();

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

        mvc.perform(post("/alltoreclaimcart")
                .with(user("owner").roles("OWNER"))
                .param("orderNumber", String.valueOf(orderNumber)));

        mvc.perform(post("/reclaimrequest")
                .with(user("owner").roles("OWNER"))
                .param("orderNumber", String.valueOf(orderNumber)));

        assertEquals(orderRepo.count(), iterableCount + 1);
        assertEquals(currentQuantity + 1, testItem.getQuantity().getAmount().intValueExact());

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
        mvc.perform(get("/staff/")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("staff"))
                .andExpect(model().attributeExists("staff"));

        mvc.perform(get("/staff")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void accConShowEmployee() throws Exception {
        UserAccountIdentifier uai = hans.getUserAccount().getIdentifier();

        mvc.perform(get("/staff/" + uai.toString())
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("isOwnProfile"))
                .andExpect(model().attributeExists("inEditingMode"));

        mvc.perform(get("/staff/" + uai.toString())
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void accConHire() throws Exception {
        mvc.perform(get("/addemployee")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("inEditingMode"))
                .andExpect(model().attributeExists("personalDataForm"));

        mvc.perform(post("/addemployee")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/staff/"));

        mvc.perform(get("/addemployee")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());

        mvc.perform(post("/addemployee")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());

    }

    @Test
    public void accConFire() throws Exception {
        UserAccountIdentifier uai = hans.getUserAccount().getIdentifier();

        mvc.perform(delete("/staff/" + uai.toString())
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/staff"));

        mvc.perform(delete("/staff/" + uai.toString())
                .with(user("erna").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void accConFireAll() throws Exception {
        mvc.perform(delete("/firestaff")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/staff"));

        mvc.perform(delete("/firestaff")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void accConProfileChangeOwner() throws Exception {
        UserAccountIdentifier uai = hans.getUserAccount().getIdentifier();

        mvc.perform(get("/staff/" + uai.toString() + "/changedata")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("personalDataForm"))
                .andExpect(model().attributeExists("isOwnProfile"))
                .andExpect(model().attributeExists("inEditingMode"));

        mvc.perform(get("/staff/" + uai.toString() + "/changepw")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("changepw"))
                .andExpect(model().attributeExists("fullname"))
                .andExpect(model().attributeExists("isOwnProfile"))
                .andExpect(model().attributeExists("passwordRules"));

        mvc.perform(get("/staff/" + uai.toString() + "/changedata")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());

        mvc.perform(get("/staff/" + uai.toString() + "/changepw")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void accConChangedData() throws Exception {
        UserAccountIdentifier uai = hans.getUserAccount().getIdentifier();

        mvc.perform(post("/staff/" + uai.toString() + "/changedata")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/staff/" + uai.toString()));

        mvc.perform(post("/staff/" + uai.toString() + "/changedata")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void accConChangedPW() throws Exception {
        UserAccountIdentifier uai = hans.getUserAccount().getIdentifier();

        mvc.perform(post("/staff/" + uai.toString() + "/changepw")
                .with(user("owner").roles("OWNER"))
                .param("newPW", "!A2s3d4f")
                .param("retypePW", "!A2s3d4f"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/staff/" + uai.toString()));

        mvc.perform(post("/staff/" + uai.toString() + "/changepw")
                .with(user("owner").roles("OWNER"))
                .param("newPW", "!A2s3d4f")
                .param("retypePW", "!A2s3d"))
                .andExpect(status().isOk())
                .andExpect(view().name("changepw"))
                .andExpect(model().attributeExists("fullname"))
                .andExpect(model().attributeExists("newPW"))
                .andExpect(model().attributeExists("retypePW"))
                .andExpect(model().attributeExists("isOwnProfile"))
                .andExpect(model().attributeExists("passwordRules"));

        mvc.perform(post("/staff/" + uai.toString() + "/changedata")
                .with(user("hans").roles("EMPLOYEE"))
                .param("newPW", "!A2s3d4f")
                .param("retypePW", "!A2s3d4f"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void accConSetPWRules() throws Exception {
        Map<String,String> map = new HashMap<>();
        map.put("minLength", "8");
        map.put("upperLower", "1");
        map.put("digits", "1");
        map.put("specialChars", "1");

        mvc.perform(get("/setrules")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("setrules"))
                .andExpect(model().attributeExists("passwordRules"))
                .andExpect(model().attributeExists("setRulesForm"));

        mvc.perform(get("/setrules")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());

        mvc.perform(post("/setrules")
                .with(user("owner").roles("OWNER"))
                .param("map", map.toString()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/staff"));

        mvc.perform(get("/setrules")
                .with(user("hans").roles("EMPLOYEE"))
                .param("map", map.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void accConProfile() throws Exception {
        mvc.perform(get("/profile")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("isOwnProfile"))
                .andExpect(model().attributeExists("inEditingMode"));

        mvc.perform(get("/profile")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void accConProfileChange() throws Exception {
        mvc.perform(get("/profile/changedata")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("personalDataForm"))
                .andExpect(model().attributeExists("isOwnProfile"))
                .andExpect(model().attributeExists("inEditingMode"));

        mvc.perform(get("/profile/changepw")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(view().name("changepw"))
                .andExpect(model().attributeExists("fullname"))
                .andExpect(model().attributeExists("isOwnProfile"))
                .andExpect(model().attributeExists("passwordRules"));

        mvc.perform(get("/profile")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void accConChangedOwnData() throws Exception {
        mvc.perform(post("/profile/changedata")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/profile"));

        mvc.perform(get("/profile")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void accConChangedOwnPW() throws Exception {
        String oldPW = "";

        mvc.perform(post("/profile/changepw")
                .with(user("hans").roles("EMPLOYEE"))
                .param("oldPW", oldPW)
                .param("newPW", "!A2s3d4f")
                .param("retypePW", "!A2s3d4f"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/profile"));

        mvc.perform(post("/profile/changepw")
                .with(user("hans").roles("EMPLOYEE"))
                .param("oldPW", oldPW)
                .param("newPW", "!A2s3d4f")
                .param("retypePW", "!A2s3d"))
                .andExpect(status().isOk())
                .andExpect(view().name("changepw"))
                .andExpect(model().attributeExists("fullname"))
                .andExpect(model().attributeExists("oldPW"))
                .andExpect(model().attributeExists("newPW"))
                .andExpect(model().attributeExists("retypePW"))
                .andExpect(model().attributeExists("isOwnProfile"))
                .andExpect(model().attributeExists("passwordRules"));

        mvc.perform(post("/profile")
                .with(user("hans").roles("INSECURE_PASSWORD"))
                .param("oldPW", oldPW)
                .param("newPW", "!A2s3d4f")
                .param("retypePW", "!A2s3d4f"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void cartConCart() throws Exception {
        mvc.perform(get("/cart")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("inventory"));

        mvc.perform(get("/cart")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        mvc.perform(post("/cart")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void cartConDeleteAll() throws Exception {
        mvc.perform(delete("/deleteallitems")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cart"));

        mvc.perform(get("/deleteallitems/")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        mvc.perform(delete("/deleteallitems")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        mvc.perform(get("/deleteallitems")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void cartConDeleteCartItem() throws Exception {
        Money price = Money.parse("EUR 5.00");
        String query = "TestProduct=1";
        long productNumber = 101;
        SuperCategory testSupCat = new SuperCategory("TestSupCat");
        supCatRepo.save(testSupCat);
        SubCategory testSubCat = new SubCategory("TestSubCat", testSupCat);
        subCatRepo.save(testSubCat);
        GSProduct testProduct = new GSProduct(productNumber, "TestProduct", price, testSubCat);

        mvc.perform(post("/cart")
                .with(user("owner").roles("OWNER"))
                .param("pid", testProduct.toString())
                .param("number", "1")
                .param("query", query));



        mvc.perform(get("/deletecartitem/"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        mvc.perform(post("/deletecartitem/")
                .with(user("owner").roles("OWNER"))
                .param("identifier", testProduct.getIdentifier().toString()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cart"));

        mvc.perform(get("/deleteallitems/")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        mvc.perform(post("/deleteallitems/")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void cartConUpdateCartItem() throws Exception {
        Money price = Money.parse("EUR 5.00");
        String query = "TestProduct=1";
        long productNumber = 101;
        SuperCategory testSupCat = new SuperCategory("TestSupCat");
        supCatRepo.save(testSupCat);
        SubCategory testSubCat = new SubCategory("TestSubCat", testSupCat);
        subCatRepo.save(testSubCat);
        GSProduct testProduct = new GSProduct(productNumber, "TestProduct", price, testSubCat);

        mvc.perform(post("/cart")
                .with(user("owner").roles("OWNER"))
                .param("pid", testProduct.toString())
                .param("number", "1")
                .param("query", query));



        mvc.perform(get("/updatecartitem/")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        mvc.perform(post("/updatecartitem/")
                .with(user("owner").roles("OWNER"))
                .param("identifier", testProduct.getIdentifier().toString())
                .param("quantity", "1"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("cart"));

        mvc.perform(get("/updatecartitem/")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        mvc.perform(post("/updatecartitem/")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void cartConCheckOut() throws Exception {
        mvc.perform(get("/checkout")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout"));

        mvc.perform(get("/checkout")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void cartConOrderOverview() throws Exception {
        mvc.perform(get("/orderoverview"))
                .andExpect(status().isOk())
                .andExpect(view().name("orderoverview"));

        mvc.perform(post("/orderoverview")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void cartConBuy() throws Exception {
        mvc.perform(get("/buy")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/productsearch"));

        mvc.perform(get("/buy")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        mvc.perform(post("/buy")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void catConSearchEntryByName() throws Exception {
        mvc.perform(get("/productsearch")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("productsearch"))
                .andExpect(model().attributeExists("catalog"))
                .andExpect(model().attributeExists("superCategories"))
                .andExpect(model().attributeExists("subCategories"))
                .andExpect(model().attributeExists("inventory"));

        mvc.perform(get("/productsearch")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void ownConOrders() throws Exception {
        mvc.perform(get("/orders")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("setOrders"))
                .andExpect(model().attributeExists("userRepo"))
                .andExpect(model().attributeExists("catalog"));
    }

    @Test
    public void ownConExportXML() throws Exception {
        mvc.perform(get("/exportxml")
                .with(user("owner").roles("OWNER"))
                .param("sort", "products"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/orders?sort=products"));

        mvc.perform(get("/exportxml")
                .with(user("owner").roles("OWNER"))
                .param("sort", "orders"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/orders?sort=orders"));
    }

    @Test
    public void ownConShowReclaim() throws Exception {
        Message message = messageRepo.findByMessageKind(MessageKind.RECLAIM).iterator().next();
        long messageID = message.getId();
        String reclaimID = message.getReclaimId();
        boolean accept = true;

        mvc.perform(post("/showreclaim/" + reclaimID)
                .with(user("owner").roles("OWNER"))
                .param("msgId", String.valueOf(messageID)))
                .andExpect(status().isOk())
                .andExpect(view().name("showreclaim"))
                .andExpect(model().attributeExists("rid"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("order"));

        mvc.perform(delete("/showreclaim/" + reclaimID)
                .with(user("owner").roles("OWNER"))
                .param("msgId", String.valueOf(messageID))
                .param("accept", String.valueOf(accept)))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/messages"));
    }

    @Test
    public void ownConJokes() throws Exception {
        long id = jokeRepo.findAll().iterator().next().getId();

        mvc.perform(get("/jokes")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("jokes"))
                .andExpect(model().attributeExists("jokes"));

        mvc.perform(get("/jokes/" + String.valueOf(id))
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("editjoke"))
                .andExpect(model().attributeExists("joke"));

        mvc.perform(delete("/jokes/" + String.valueOf(id))
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/jokes"));
    }

    @Test
    public void ownConNewJoke() throws Exception {
        String jokeText = "testjoke";

        mvc.perform(get("/newjoke")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("editjoke"));

        mvc.perform(post("/newjoke")
                .with(user("owner").roles("OWNER"))
                .param("jokeText", jokeText))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/jokes"));
    }

    @Test
    public void ownConEditJoke() throws Exception {
        long id = jokeRepo.findAll().iterator().next().getId();
        String jokeText = "testjoke";

        mvc.perform(post("/editjoke/" + String.valueOf(id))
                .with(user("owner").roles("OWNER"))
                .param("jokeText", jokeText))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/jokes"));
    }

    @Test
    public void ownConDeleteAllJokes() throws Exception {
        mvc.perform(delete("/deljokes")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/jokes"));
    }

    @Test
    public void ownConMessages() throws Exception {
        long id = messageRepo.findAll().iterator().next().getId();

        mvc.perform(get("/messages")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("messages"))
                .andExpect(model().attributeExists("ownermessage"));

        mvc.perform(delete("/messages/" + String.valueOf(id))
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/messages"));
    }

    @Test
    public void ownConRange() throws Exception {
        mvc.perform(get("/range")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("range"))
                .andExpect(model().attributeExists("inventory"))
                .andExpect(model().attributeExists("supercategories"));
    }

    @Test
    public void ownConRangeDel() throws Exception {
        String superName = supCatRepo.findAll().iterator().next().getName();
        String subName = subCatRepo.findAll().iterator().next().getName();
        ProductIdentifier productID = catalog.findAll().iterator().next().getIdentifier();

        mvc.perform(delete("/range/delsuper")
                .with(user("owner").roles("OWNER"))
                .param("superName", superName))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));

        mvc.perform(delete("/range/delsub")
                .with(user("owner").roles("OWNER"))
                .param("subName", subName))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));

        mvc.perform(delete("/range/delproduct")
                .with(user("owner").roles("OWNER"))
                .param("productIdent", productID.toString()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));
    }

    @Test
    public void ownConEditProduct() throws Exception {
        ProductIdentifier productID = catalog.findAll().iterator().next().getIdentifier();

        mvc.perform(get("/range/editproduct/" + productID.toString())
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("editproduct"))
                .andExpect(model().attributeExists("productForm"))
                .andExpect(model().attributeExists("productName"))
                .andExpect(model().attributeExists("superCategories"))
                .andExpect(model().attributeExists("isNew"));

        mvc.perform(post("/range/editproduct/" + productID.toString())
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));
    }

    @Test
    public void ownConAddProduct() throws Exception {
        mvc.perform(get("/range/addproduct")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("editproduct"))
                .andExpect(model().attributeExists("productForm"))
                .andExpect(model().attributeExists("superCategories"))
                .andExpect(model().attributeExists("isNew"));

        mvc.perform(post("/range/addproduct")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));
    }

    @Test
    public void ownConEditSuperCategory() throws Exception {
        String superName = supCatRepo.findAll().iterator().next().getName();
        String name = subCatRepo.findAll().iterator().next().getName();

        mvc.perform(get("/range/editsuper/" + superName)
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("editsuper"))
                .andExpect(model().attributeExists("super"))
                .andExpect(model().attributeExists("name"));

        mvc.perform(post("/range/editsuper/" + superName)
                .with(user("owner").roles("OWNER"))
                .param("name", name))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));

        mvc.perform(post("/range/editsuper/" + superName)
                .with(user("owner").roles("OWNER"))
                .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("editsuper"))
                .andExpect(model().attributeExists("super"))
                .andExpect(model().attributeExists("name"))
                .andExpect(model().attributeExists("nameError"));

        mvc.perform(post("/range/editsuper/" + superName)
                .with(user("owner").roles("OWNER"))
                .param("name", superName))
                .andExpect(status().isOk())
                .andExpect(view().name("editsuper"))
                .andExpect(model().attributeExists("super"))
                .andExpect(model().attributeExists("name"))
                .andExpect(model().attributeExists("nameError"));
    }

    @Test
    public void ownConAddSuper() throws Exception {
        String name = "testName";

        mvc.perform(get("/range/addsuper")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("editsuper"));

        mvc.perform(post("/range/addsuper")
                .with(user("owner").roles("OWNER"))
                .param("name", name))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));

        mvc.perform(post("/range/addsuper")
                .with(user("owner").roles("OWNER"))
                .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("editsuper"))
                .andExpect(model().attributeExists("name"))
                .andExpect(model().attributeExists("nameError"));
    }

    @Test
    public void ownConEditSubCategory() throws Exception {
        String sub = subCatRepo.findAll().iterator().next().getName();
        String newSub = "newTestName";

        mvc.perform(get("/range/editsub/" + sub)
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("editsub"))
                .andExpect(model().attributeExists("sub"))
                .andExpect(model().attributeExists("superCategory"))
                .andExpect(model().attributeExists("name"))
                .andExpect(model().attributeExists("superCategories"));

        mvc.perform(get("/range/editsub/notExisting")
                .with(user("owner"). roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));

        mvc.perform(post("/range/editsub/" + sub)
                .with(user("owner").roles("OWNER"))
                .param("name", newSub))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));

        mvc.perform(post("/range/editsub/notExisting")
                .with(user("owner").roles("OWNER"))
                .param("name", newSub))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));

        mvc.perform(post("/range/editsub/" + sub)
                .with(user("owner").roles("OWNER"))
                .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("editsub"))
                .andExpect(model().attributeExists("sub"))
                .andExpect(model().attributeExists("superCategory"))
                .andExpect(model().attributeExists("name"))
                .andExpect(model().attributeExists("superCategories"));
    }

    @Test
    public void ownConAddSubCategory() throws Exception {
        String name = "newSub";

        mvc.perform(get("/range/addsub")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("editsub"))
                .andExpect(model().attributeExists("superCategories"));

        mvc.perform(post("/range/addsub")
                .with(user("owner").roles("OWNER"))
                .param("name", name))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));

        mvc.perform(post("/range/addsub")
                .with(user("owner").roles("OWNER"))
                .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("editsub"))
                .andExpect(model().attributeExists("superCategory"))
                .andExpect(model().attributeExists("name"))
                .andExpect(model().attributeExists("superCategories"))
                .andExpect(model().attributeExists("nameError"));
    }

    @Test
    public void recConReclaim() throws Exception {
        mvc.perform(get("/reclaim")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("reclaim"))
                .andExpect(model().attributeExists("catalog"));

        mvc.perform(post("/reclaim")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void recConReclaimCart() throws Exception {
        GSOrder order = orderRepo.findByType(OrderType.NORMAL).iterator().next();
        long num = order.getOrderNumber();
        GSProduct product = catalog.findByName(order.getOrderLines().iterator().next().getProductName()).iterator().next();
        ProductIdentifier productID = product.getIdentifier();
        SalespointIdentifier sID = order.findOrderLineByProduct(product).getIdentifier();
        int reclaimNum = (int)orderRepo.findByType(OrderType.RECLAIM).iterator().next().getOrderNumber();

        mvc.perform(get("/reclaimcart")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        mvc.perform(post("/reclaimcart")
                .with(user("owner").roles("OWNER"))
                .param("orderNumber", String.valueOf(num))
                .param("rpid", productID.toString())
                .param("olid", sID.toString())
                .param("rnumber", String.valueOf(reclaimNum)))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/reclaim"))
                .andExpect(model().attributeExists("orderNumber"))
                .andExpect(model().attributeExists("reclaimorder"));

        mvc.perform(get("/reclaimcart")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        mvc.perform(post("/relaimcart")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void recConAllToReclaimCart() throws Exception {
        long num = orderRepo.findByType(OrderType.NORMAL).iterator().next().getOrderNumber();

        mvc.perform(post("/alltoreclaimcart")
                .with(user("owner").roles("OWNER"))
                .param("orderNumber", String.valueOf(num)))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("orderNumber"));

        mvc.perform(post("/alltoreclaimcart")
                .with(user("hans").roles("INSECURE_PASSWORD"))
                .param("orderNumber", String.valueOf(num)))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void recConReclaimIt() throws Exception {
        long num = orderRepo.findByType(OrderType.NORMAL).iterator().next().getOrderNumber();

        mvc.perform(post("/reclaimrequest")
                .with(user("owner").roles("OWNER"))
                .param("orderNumber", String.valueOf(num)))
                .andExpect(status().isOk())
                .andExpect(view().name("orderoverview"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("catalog"));

        mvc.perform(post("/reclaimrequest")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void recConSearchOrderByNumber() throws Exception {
        String searchOrderNumber = String.valueOf(orderRepo.findByType(OrderType.NORMAL).iterator().next().getOrderNumber());

        mvc.perform(get("/ordersearch")
                .with(user("owner").roles("OWNER"))
                .param("searchordernumber", searchOrderNumber))
                .andExpect(status().isOk())
                .andExpect(view().name("reclaim"))
                .andExpect(model().attributeExists("reclaimorder"))
                .andExpect(model().attributeExists("catalog"));

        mvc.perform(post("/ordersearch")
                .with(user("owner").roles("OWNER"))
                .param("searchordernumber", "test"))
                .andExpect(status().isOk())
                .andExpect(view().name("reclaim"))
                .andExpect(model().attributeExists("error"));

        mvc.perform(post("/ordersearch")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void recConCheckOut() throws Exception {
        String orderNum = String.valueOf(orderRepo.findByType(OrderType.NORMAL).iterator().next().getOrderNumber());

        mvc.perform(get("/rcheckout")
                .with(user("owner").roles("OWNER"))
                .param("orderNumber", orderNum))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout"));

        mvc.perform(get("/rcheckout")
                .with(user("hans").roles("INSECURE_PASSWORD"))
                .param("orderNumber", orderNum))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void recConUpdateReclaimCartItem() throws Exception {
        GSOrder reclaim = orderRepo.findByType(OrderType.RECLAIM).iterator().next();
        String identifier = reclaim.getIdentifier().toString();
        String quantity = "1";

        mvc.perform(post("/updatereclaimcartitem/")
                .with(user("owner").roles("OWNER"))
                .param("identifier", identifier)
                .param("quantity", quantity))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("cart"));

        mvc.perform(post("/updatereclaimcartitem/")
                .with(user("hans").roles("INSECURE_PASSWORD"))
                .param("identifier", identifier)
                .param("quantity", quantity))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void recConCancelReclaim() throws Exception {
        mvc.perform(get("/cancelreclaim")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/reclaim"));

        mvc.perform(post("/cancelreclaim")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/reclaim"));

        mvc.perform(get("/cancelreclaim")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        mvc.perform(post("/cancelreclaim")
                .with(user("hans").roles("INSECURE_PASSWORD")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }


}
