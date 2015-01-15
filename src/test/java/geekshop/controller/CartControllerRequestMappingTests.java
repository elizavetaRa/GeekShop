package geekshop.controller;

import geekshop.AbstractWebIntegrationTests;
import geekshop.model.*;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.quantity.Units;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Midokin on 15.01.2015.
 */
public class CartControllerRequestMappingTests extends AbstractWebIntegrationTests{

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

    private User hans;
    private GSInventoryItem testItem;

    @Before
    public void setup() {
        hans = userRepo.findByUserAccount(uam.findByUsername("hans").get());

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
                .with(user("hans").roles("EMPLOYEE"))
                .param("pid", testProduct.getId().toString())
                .param("number", "1")
                .param("query", query)
                .sessionAttr("isReclaim", false))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/productsearch?" + query))
                .andReturn().getModelAndView().getModel().get("cart");

        mvc.perform((post("/buy")
                .with(user("hans").roles("EMPLOYEE"))
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
                .with(user("hans").roles("EMPLOYEE"))
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
                .with(user("hans").roles("EMPLOYEE"))
                .sessionAttr("isReclaim", false))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cart"));

        mvc.perform(get("/deleteallitems/")
                .with(user("hans").roles("EMPLOYEE")))
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
                .with(user("hans").roles("EMPLOYEE"))
                .param("pid", testProduct.getId().toString())
                .param("number", "1")
                .param("query", query)
                .sessionAttr("isReclaim", false));


        mvc.perform(get("/deletecartitem/")
                .with(user("hans").roles("EMPLOYEE"))
                .sessionAttr("isReclaim", false))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        mvc.perform(post("/deletecartitem/")
                .with(user("hans").roles("EMPLOYEE"))
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
                .with(user("hans").roles("EMPLOYEE"))
                .param("pid", testProduct.getId().toString())
                .param("number", "1")
                .param("query", query)
                .sessionAttr("isReclaim", false))
                .andReturn().getModelAndView().getModel().get("cart");

        CartItem testCartItem = cart.iterator().next();

        mvc.perform(get("/updatecartitem/")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        mvc.perform(post("/updatecartitem/")
                .with(user("hans").roles("EMPLOYEE"))
                .param("identifier", testCartItem.getIdentifier())
                .param("quantity", "1")
                .sessionAttr("cart", cart))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cart"));

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(get("/updatecartitem/")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        mvc.perform(post("/updatecartitem/")
                .with(user("hans").roles("EMPLOYEE"))
                .param("identifier", testCartItem.getIdentifier())
                .param("quantity", "1")
                .sessionAttr("cart", cart))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void cartConCheckOut() throws Exception {
        mvc.perform(get("/checkout")
                .with(user("hans").roles("EMPLOYEE")))
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
    public void cartConBuy() throws Exception {
        mvc.perform(get("/buy")
                .with(user("hans").roles("EMPLOYEE")))
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
}
