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
import org.salespointframework.order.Cart;
import org.salespointframework.order.OrderLine;
import org.salespointframework.quantity.Units;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccountIdentifier;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.math.BigDecimal;
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
public class ControllerIntegrationTests extends AbstractWebIntegrationTests {


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
        catalog.save(testProduct);
        testItem = new GSInventoryItem(testProduct, Units.TEN, Units.of(5L));
        inventory.save(testItem);

        assertNotNull(supCatRepo.findByName("TestSupCat"));
        assertNotNull(subCatRepo.findByName("TestSubCat"));
        assertNotNull(inventory.findByProduct(testProduct));

        Cart cart = (Cart) mvc.perform(post("/cart")
                .with(user("owner").roles("OWNER"))
                .param("pid", testProduct.getId().toString())
                .param("number", "1")
                .param("query", query)
                .sessionAttr("isReclaim", false))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/productsearch?" + query))
                .andReturn().getModelAndView().getModel().get("cart");

        mvc.perform((post("/buy")
                .with(user("owner").roles("OWNER"))
                .param("payment", payment))
                .sessionAttr("cart", cart)
                .sessionAttr("isReclaim", false))
                .andExpect(status().isOk())
                .andExpect(view().name("orderoverview"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("catalog"));

        boolean found = false;
        for (GSOrder order : orderRepo.findByType(OrderType.NORMAL)) {
            if (!order.isOpen() && !order.isCanceled()) {
                if (order.findOrderLineByProduct(testProduct) != null) {
                    found = true;
                }
            }
        }

        assertTrue(found);

        assertTrue(testItem.getQuantity().getAmount().intValue() == 9);

    }

    @Test
    @SuppressWarnings("unchecked")
    public void reclaimSomething() throws Exception {

        GSOrder order = null;
        GSProduct reclProduct = null;

        for (GSOrder o : orderRepo.findByType(OrderType.NORMAL)) {
            if (!o.isOpen() && !o.isCanceled()) {
                order = o;
                boolean exitLoop = false;
                for (OrderLine ol : order.getOrderLines()) {
                    if (inventory.findByProductIdentifier(ol.getProductIdentifier()).get().getQuantity().getAmount().intValue() > 1) {
                        reclProduct = catalog.findOne(ol.getProductIdentifier()).get();
                        exitLoop = true;
                        break;
                    }
                }
                if (exitLoop)
                    break;
            }
        }

        if (order == null || reclProduct == null)
            return;

        long orderNumber = order.getOrderNumber();
        long iterableCount = orderRepo.count();
        int productQuantity = inventory.findByProduct(reclProduct).get().getQuantity().getAmount().intValue();

        Map<GSProduct, BigDecimal> mapAmounts = (Map<GSProduct, BigDecimal>)
                mvc.perform(get("/ordersearch")
                        .with(user("owner").roles("OWNER"))
                        .param("searchordernumber", String.valueOf(orderNumber))
                        .sessionAttr("isReclaim", true))
                        .andReturn().getRequest().getSession().getAttribute("mapAmounts");

        Cart cart = (Cart) mvc.perform(post("/reclaimcart")
                .with(user("owner").roles("OWNER"))
                .param("orderNumber", String.valueOf(orderNumber))
                .param("rpid", reclProduct.getId().toString())
                .param("rnumber", "2")
                .sessionAttr("isReclaim", true)
                .sessionAttr("mapAmounts", mapAmounts))
                .andReturn().getModelAndView().getModel().get("cart");

        mvc.perform(post("/reclaimrequest")
                .with(user("owner").roles("OWNER"))
                .param("orderNumber", String.valueOf(orderNumber))
                .sessionAttr("cart", cart)
                .sessionAttr("overview", true)
                .sessionAttr("oN", String.valueOf(orderNumber)));

        assertEquals(orderRepo.count(), iterableCount + 1);
        assertEquals(productQuantity, inventory.findByProduct(reclProduct).get().getQuantity().getAmount().intValueExact());
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
        String newUserName = "hans1";

        mvc.perform(get("/addemployee")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("inEditingMode"))
                .andExpect(model().attributeExists("personalDataForm"));

        mvc.perform(post("/addemployee")
                .with(user("owner").roles("OWNER"))
                .param("firstname", "hans")
                .param("lastname", "hansen")
                .param("username", newUserName)
                .param("email", "hans@hansen.de")
                .param("gender", "MALE")
                .param("dateOfBirth", "01.01.1990")
                .param("maritalStatus", "UNMARRIED")
                .param("phone", "01231234567")
                .param("street", "hansstreet")
                .param("houseNr", "1")
                .param("postcode", "01234")
                .param("place", "hansstadt"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/staff/" + newUserName));

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
                .with(user("owner").roles("OWNER"))
                .param("firstname", "hans")
                .param("lastname", "hansen")
                .param("username", uai.toString())
                .param("email", "hans@hansen.de")
                .param("gender", "MALE")
                .param("dateOfBirth", "01.01.1990")
                .param("maritalStatus", "UNMARRIED")
                .param("phone", "01231234567")
                .param("street", "hansstreet")
                .param("houseNr", "1")
                .param("postcode", "01234")
                .param("place", "hansstadt"))
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
        Map<String, String> map = new HashMap<>();
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
                .param("minLength", "8")
                .param("upperLower", "1")
                .param("digits", "1")
                .param("specialChars", "1"))
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

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(get("/profile")
                .with(user("hans").roles("EMPLOYEE")))
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

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(get("/profile")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void accConChangedOwnData() throws Exception {
        mvc.perform(post("/profile/changedata")
                .with(user("hans").roles("EMPLOYEE"))
                .param("firstname", "hans")
                .param("lastname", "hansen")
                .param("username", "hans")
                .param("email", "hans@hansen.de")
                .param("gender", "MALE")
                .param("dateOfBirth", "01.01.1990")
                .param("maritalStatus", "UNMARRIED")
                .param("phone", "01231234567")
                .param("street", "hansstreet")
                .param("houseNr", "1")
                .param("postcode", "01234")
                .param("place", "hansstadt"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/profile"));

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(get("/profile")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void accConChangedOwnPW() throws Exception {
        String oldPW = "123";

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

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(post("/profile")
                .with(user("hans").roles("EMPLOYEE"))
                .param("oldPW", oldPW)
                .param("newPW", "!A2s3d4f")
                .param("retypePW", "!A2s3d4f"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void cartConCart() throws Exception {
        Money price = Money.parse("EUR 5.00");
        String query = "q=aufkl&sort=name&cat=";
        long productNumber = 101;
        SuperCategory testSupCat = new SuperCategory("TestSupCat");
        supCatRepo.save(testSupCat);
        SubCategory testSubCat = new SubCategory("TestSubCat", testSupCat);
        subCatRepo.save(testSubCat);
        GSProduct testProduct = new GSProduct(productNumber, "TestProduct", price, testSubCat);

        mvc.perform(get("/cart")
                .with(user("owner").roles("OWNER"))
                .sessionAttr("isReclaim", false))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("inventory"));

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(get("/cart")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        mvc.perform(post("/cart")
                .with(user("hans").roles("EMPLOYEE"))
                .param("pid", testProduct.getId().toString())
                .param("number", "1")
                .param("query", query)
                .sessionAttr("isReclaim", false))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void cartConDeleteAll() throws Exception {
        mvc.perform(delete("/deleteallitems")
                .with(user("owner").roles("OWNER"))
                .sessionAttr("isReclaim", false))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cart"));

        mvc.perform(get("/deleteallitems/")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(delete("/deleteallitems")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        mvc.perform(get("/deleteallitems/")
                .with(user("hans").roles("EMPLOYEE")))
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
        catalog.save(testProduct);
        testItem = new GSInventoryItem(testProduct, Units.TEN, Units.of(5L));
        inventory.save(testItem);

        mvc.perform(post("/cart")
                .with(user("owner").roles("OWNER"))
                .param("pid", testProduct.getId().toString())
                .param("number", "1")
                .param("query", query)
                .sessionAttr("isReclaim", false));


        mvc.perform(get("/deletecartitem/")
                .with(user("owner").roles("OWNER"))
                .sessionAttr("isReclaim", false))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        mvc.perform(post("/deletecartitem/")
                .with(user("owner").roles("OWNER"))
                .param("identifier", testProduct.getIdentifier().toString())
                .sessionAttr("isReclaim", false))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cart"));

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(get("/deleteallitems/")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        mvc.perform(post("/deleteallitems/")
                .with(user("hans").roles("EMPLOYEE")))
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
        catalog.save(testProduct);
        testItem = new GSInventoryItem(testProduct, Units.TEN, Units.of(5L));
        inventory.save(testItem);

        Cart cart = (Cart) mvc.perform(post("/cart")
                .with(user("owner").roles("OWNER"))
                .param("pid", testProduct.getId().toString())
                .param("number", "1")
                .param("query", query)
                .sessionAttr("isReclaim", false))
                .andReturn().getModelAndView().getModel().get("cart");


        mvc.perform(get("/updatecartitem/")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        mvc.perform(post("/updatecartitem/")
                .with(user("owner").roles("OWNER"))
                .param("identifier", testItem.getIdentifier().getIdentifier())
                .param("quantity", "1")
                .sessionAttr("cart", cart))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("cart"));

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(get("/updatecartitem/")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        mvc.perform(post("/updatecartitem/")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void cartConCheckOut() throws Exception {
        mvc.perform(get("/checkout")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout"));

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(get("/checkout")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void cartConOrderOverview() throws Exception {
        long orderNumber = orderRepo.findByType(OrderType.NORMAL).iterator().next().getOrderNumber();

        mvc.perform(get("/orderoverview")
                .with(user("owner").roles("OWNER"))
                .sessionAttr("overview", false)
                .sessionAttr("orderNumber", orderNumber))
                .andExpect(status().isOk())
                .andExpect(view().name("orderoverview"));

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(get("/orderoverview")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void cartConBuy() throws Exception {
        mvc.perform(get("/buy")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/productsearch"));

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(get("/buy")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        mvc.perform(post("/buy")
                .with(user("hans").roles("EMPLOYEE"))
                .param("payment", "CASH")
                .sessionAttr("isReclaim", false))
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

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(get("/productsearch")
                .with(user("hans").roles("EMPLOYEE")))
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
        ProductIdentifier productID = catalog.findAll().iterator().next().getIdentifier();

        mvc.perform(delete("/range/delsuper")
                .with(user("owner").roles("OWNER"))
                .param("superName", "Kleidung"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));

        mvc.perform(delete("/range/delsub")
                .with(user("owner").roles("OWNER"))
                .param("subName", "Informatik"))
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
                .with(user("owner").roles("OWNER"))
                .param("name", "Täst")
                .param("productNumber", "1234567")
                .param("price", "1.234.567,89 €")
                .param("minQuantity", "5")
                .param("quantity", "10")
                .param("subCategory", "Informatik"))
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
                .with(user("owner").roles("OWNER"))
                .param("name", "Täst")
                .param("productNumber", "1234567")
                .param("price", "1.234.567,89 €")
                .param("minQuantity", "5")
                .param("quantity", "10")
                .param("subCategory", "Informatik"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));
    }

    @Test
    public void ownConEditSuperCategory() throws Exception {
        String superName = supCatRepo.findAll().iterator().next().getName();

        mvc.perform(get("/range/editsuper/" + superName)
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("editsuper"))
                .andExpect(model().attributeExists("super"))
                .andExpect(model().attributeExists("name"));

        mvc.perform(post("/range/editsuper/" + superName)
                .with(user("owner").roles("OWNER"))
                .param("name", "newSuperName"))
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
                .param("name", "Kleidung"))
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
        String newSub = "newTestName";

        mvc.perform(get("/range/editsub/Informatik")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("editsub"))
                .andExpect(model().attributeExists("sub"))
                .andExpect(model().attributeExists("superCategory"))
                .andExpect(model().attributeExists("name"))
                .andExpect(model().attributeExists("superCategories"));

        mvc.perform(get("/range/editsub/notExisting")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));

        mvc.perform(post("/range/editsub/Informatik")
                .with(user("owner").roles("OWNER"))
                .param("name", newSub)
                .param("superCategory", "Kleidung"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));

        mvc.perform(post("/range/editsub/notExisting")
                .with(user("owner").roles("OWNER"))
                .param("name", newSub)
                .param("superCategory", "Kleidung"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));

        mvc.perform(post("/range/editsub/Informatik")
                .with(user("owner").roles("OWNER"))
                .param("name", "")
                .param("superCategory", ""))
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
                .param("name", name)
                .param("superCategory", "Kleidung"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));

        mvc.perform(post("/range/addsub")
                .with(user("owner").roles("OWNER"))
                .param("name", "")
                .param("superCategory", ""))
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

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(post("/reclaim")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void recConReclaimCart() throws Exception {
        GSOrder order = orderRepo.findByType(OrderType.NORMAL).iterator().next();
        long num = order.getOrderNumber();
        GSProduct product = catalog.findByName(order.getOrderLines().iterator().next().getProductName()).iterator().next();
        ProductIdentifier productID = product.getIdentifier();
        int reclaimNum = (int) orderRepo.findByType(OrderType.RECLAIM).iterator().next().getOrderNumber();
        long orderNumber = order.getOrderNumber();

        mvc.perform(get("/reclaimcart")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        Map<GSProduct, BigDecimal> mapAmounts = (Map<GSProduct, BigDecimal>)
                mvc.perform(get("/ordersearch")
                        .with(user("owner").roles("OWNER"))
                        .param("searchordernumber", String.valueOf(orderNumber))
                        .sessionAttr("isReclaim", true))
                        .andReturn().getRequest().getSession().getAttribute("mapAmounts");

        mvc.perform(post("/reclaimcart")
                .with(user("owner").roles("OWNER"))
                .param("orderNumber", String.valueOf(num))
                .param("rpid", productID.toString())
                .param("rnumber", String.valueOf(reclaimNum))
                .sessionAttr("isReclaim", true)
                .sessionAttr("mapAmounts", mapAmounts))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/reclaim?orderNumber=" + String.valueOf(num)))
                .andExpect(model().attributeExists("orderNumber"))
                .andExpect(model().attributeExists("reclaimorder"));

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(get("/reclaimcart")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        mvc.perform(post("/relaimcart")
                .with(user("hans").roles("EMPLOYEE"))
                .param("orderNumber", String.valueOf(num))
                .param("rpid", productID.toString())
                .param("rnumber", String.valueOf(reclaimNum))
                .sessionAttr("isReclaim", true)
                .sessionAttr("mapAmounts", mapAmounts))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void recConAllToReclaimCart() throws Exception {
        long num = orderRepo.findByType(OrderType.NORMAL).iterator().next().getOrderNumber();

        Map<GSProduct, BigDecimal> mapAmounts = (Map<GSProduct, BigDecimal>)
                mvc.perform(get("/ordersearch")
                        .with(user("owner").roles("OWNER"))
                        .param("searchordernumber", String.valueOf(num))
                        .sessionAttr("isReclaim", true))
                        .andReturn().getRequest().getSession().getAttribute("mapAmounts");

        mvc.perform(post("/alltoreclaimcart")
                .with(user("owner").roles("OWNER"))
                .param("orderNumber", String.valueOf(num))
                .sessionAttr("isReclaim", true)
                .sessionAttr("mapAmounts", mapAmounts))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("orderNumber"));

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(post("/alltoreclaimcart")
                .with(user("hans").roles("EMPLOYEE"))
                .param("orderNumber", String.valueOf(num)))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void recConReclaimIt() throws Exception {
        long num = orderRepo.findByType(OrderType.NORMAL).iterator().next().getOrderNumber();

        mvc.perform(post("/reclaimrequest")
                .with(user("owner").roles("OWNER"))
                .param("orderNumber", String.valueOf(num))
                .sessionAttr("overview", false))
                .andExpect(status().isOk())
                .andExpect(view().name("orderoverview"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("catalog"));

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(post("/reclaimrequest")
                .with(user("hans").roles("EMPLOYEE"))
                .param("orderNumber", String.valueOf(num)))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void recConSearchOrderByNumber() throws Exception {
        String searchOrderNumber = String.valueOf(orderRepo.findByType(OrderType.NORMAL).iterator().next().getOrderNumber());

        mvc.perform(get("/ordersearch")
                .with(user("owner").roles("OWNER"))
                .param("searchordernumber", searchOrderNumber)
                .sessionAttr("isReclaim", false))
                .andExpect(status().isOk())
                .andExpect(view().name("reclaim"))
                .andExpect(model().attributeExists("reclaimorder"))
                .andExpect(model().attributeExists("catalog"));

        mvc.perform(get("/ordersearch")
                .with(user("owner").roles("OWNER"))
                .param("searchordernumber", "test")
                .sessionAttr("isReclaim", false))
                .andExpect(status().isOk())
                .andExpect(view().name("reclaim"))
                .andExpect(model().attributeExists("error"));

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(get("/ordersearch")
                .with(user("hans").roles("EMPLOYEE"))
                .param("searchordernumber", searchOrderNumber))
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

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(get("/rcheckout")
                .with(user("hans").roles("EMPLOYEE"))
                .param("orderNumber", orderNum))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void recConUpdateReclaimCartItem() throws Exception {
        GSOrder reclaim = orderRepo.findByType(OrderType.RECLAIM).iterator().next();
        String identifier = reclaim.getIdentifier().toString();
        String quantity = "1";

        GSOrder order = null;
        GSProduct reclProduct = null;

        for (GSOrder o : orderRepo.findByType(OrderType.NORMAL)) {
            if (!o.isOpen() && !o.isCanceled()) {
                order = o;
                boolean exitLoop = false;
                for (OrderLine ol : order.getOrderLines()) {
                    if (inventory.findByProductIdentifier(ol.getProductIdentifier()).get().getQuantity().getAmount().intValue() > 1) {
                        reclProduct = catalog.findOne(ol.getProductIdentifier()).get();
                        exitLoop = true;
                        break;
                    }
                }
                if (exitLoop)
                    break;
            }
        }

        if (order == null || reclProduct == null)
            return;

        long orderNumber = order.getOrderNumber();

        Map<GSProduct, BigDecimal> mapAmounts = (Map<GSProduct, BigDecimal>)
                mvc.perform(get("/ordersearch")
                        .with(user("owner").roles("OWNER"))
                        .param("searchordernumber", String.valueOf(orderNumber))
                        .sessionAttr("isReclaim", true))
                        .andReturn().getRequest().getSession().getAttribute("mapAmounts");

        Cart cart = (Cart) mvc.perform(post("/reclaimcart")
                .with(user("owner").roles("OWNER"))
                .param("orderNumber", String.valueOf(orderNumber))
                .param("rpid", reclProduct.getId().toString())
                .param("rnumber", "2")
                .sessionAttr("isReclaim", true)
                .sessionAttr("mapAmounts", mapAmounts))
                .andReturn().getModelAndView().getModel().get("cart");


        mvc.perform(post("/updatereclaimcartitem/")
                .with(user("owner").roles("OWNER"))
                .param("identifier", identifier)
                .param("quantity", quantity)
                .sessionAttr("oN", orderNumber)
                .sessionAttr("cart", cart))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("cart"));

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(post("/updatereclaimcartitem/")
                .with(user("hans").roles("EMPLOYEE"))
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

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(get("/cancelreclaim")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        mvc.perform(post("/cancelreclaim")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }


}
