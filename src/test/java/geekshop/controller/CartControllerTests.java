package geekshop.controller;


import geekshop.AbstractWebIntegrationTests;
import geekshop.model.*;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.Product;
import org.salespointframework.core.SalespointIdentifier;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.order.Cart;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.salespointframework.order.CartItem;
import org.salespointframework.quantity.Metric;
import org.salespointframework.quantity.Units;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Metrics;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.SessionAttributes;


import javax.servlet.http.HttpSession;

import java.util.Optional;

public class CartControllerTests extends AbstractWebIntegrationTests {

    @Autowired
    CartController controller;
    @Autowired
    CatalogController catController;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    GSOrderRepository gsOrderRepository;
    @Autowired
    Catalog<GSProduct> catalog;
    @Autowired
    OwnerController ownerController;
    @Autowired
    HttpSession session;
    @Autowired
    SubCategoryRepository subCatRepo;
    @Autowired
    SuperCategoryRepository superCatRepo;
    @Autowired
    Inventory inventory;
    @Autowired
    UserRepository userRepo;




    private GSInventoryItem item1;
    private GSInventoryItem item2;
    private GSInventoryItem item3;
    private GSInventoryItem item4;
    private GSInventoryItem item5;
    private GSInventoryItem item6;

    Cart cart;
    Model model;
    UserAccount userAccount;
//    MockHttpSession session= new MockHttpSession();


    @Before
    public void setUp() {
        login("owner", "123");
        userAccount= userRepo.findAll().iterator().next().getUserAccount();
        cart = controller.initializeCart();
        model = new ExtendedModelMap();
        SuperCategory superCategory = new SuperCategory("superCat");
        superCatRepo.save(superCategory);
        SubCategory subCategory = new SubCategory("subCat", superCategory);
        subCatRepo.save(subCategory);
        GSProduct prod1 = new GSProduct(100, "test1", Money.of(CurrencyUnit.EUR, 1D), subCategory);
        GSProduct prod2 = new GSProduct(101, "test2", Money.of(CurrencyUnit.EUR, 2D), subCategory);
        GSProduct prod3 = new GSProduct(102, "test3", Money.of(CurrencyUnit.EUR, 3D), subCategory);
        item1 = new GSInventoryItem(prod1, Units.TEN, Units.of(5L));
        item2 = new GSInventoryItem(prod2, Units.TEN, Units.of(5L));
        item3 = new GSInventoryItem(prod3, Units.TEN, Units.of(5L));
        inventory.save(item1);
        inventory.save(item2);
        inventory.save(item3);
        catalog.save(prod1);
        catalog.save(prod2);
        catalog.save(prod3);
        session.setAttribute("isReclaim", false);

    }

    //tests if the product is added to cart
    @Test
    public void addProductToCart() throws Exception {

        cart.clear();
        GSProduct product = catalog.findByName("test1").iterator().next();
        controller.addProductToCart(product, 9, "1", cart, session, Optional.of(userAccount), model);
       // long id = product.getProductNumber();
        assertTrue(testCartItem());
        cart.clear();
        controller.addProductToCart(product, -42, "1", cart, session, Optional.of(userAccount), model);
        assertTrue(testCartItem2());
        cart.clear();
        controller.addProductToCart(product, 102, "1", cart, session, Optional.of(userAccount), model);  //more products than in inventory
        assertEquals(cart.iterator().next().getQuantity().getAmount().intValue(), 10);
        cart.clear();
        //more products than in inventory with multiple using of function
        controller.addProductToCart(product, 3, "1", cart, session, Optional.of(userAccount), model);
        controller.addProductToCart(product, 8, "1", cart, session, Optional.of(userAccount), model);
        assertEquals(cart.iterator().next().getQuantity().getAmount().intValue(), 10);

    }

    public Boolean testCartItem() {
        CartItem item = cart.iterator().next();
        if (item == null) return false;
         else {
            if (item.getQuantity().getAmount().intValueExact() == 9) {
                return true;
            } return false;
        }
    }

    public Boolean testCartItem2() {
        if (cart.isEmpty()) {
            return true;
        } else return false;
        }



//    // tests if cart has correct amount of products
//    @Test
//    public void addProductToCart2() throws Exception {
//        Model model = new ExtendedModelMap();
//
////        org.joda.money.Money==
////        Product product=new Product()
//        ownerController.addProductToCatalog(model, product, 1l, 12, 1, 1);
//        GSProduct product = catalog.findByName("Test").iterator().next();
//        controller.addProductToCart(product, -100, "1", cart, session, user);
//        long id = product.getProductNumber();
//        assertTrue(testItemAmount());
//    }
//
//    public Boolean testItemAmount() {
//        CartItem item = cart.iterator().next();
//        if (cart.isEmpty()) {
//            return true;
//        } else return false;
//    }

//
////    // tests if correct CartItem is deleted
////    @Test
////    public void deleteCartItem() throws Exception {
////        Model model = new ExtendedModelMap();
////
//////        org.joda.money.Money==
//////        Product product=new Product()
////        ownerController.addProductToCatalog(model, product, 1l, 12, 1, 1);
////        GSProduct product= catalog.findByName("Test").iterator().next();
////        controller.addProductToCart(product, 3, "1", cart, session, user);
////        cart.
////    }
////
////
////    public Boolean testCartItem3(String id) {
////        CartItem item= cart.getItem(id);
////        if (cart.isEmpty()) {
////            return true;
////        } else {
////            if (item.getQuantity().getAmount().intValueExact() == -100) {
////                cart.clear();
////                return false;
////            }
////        }
////    }
//
//
//    // tests if CartItem is correctly updated
//    @Test
//    public void updateCartItem() throws Exception {
//        Model model = new ExtendedModelMap();
//
////        org.joda.money.Money==
////        Product product=new Product()
//        ownerController.addProductToCatalog(model, product, 1l, 12, 1, 1);
//        GSProduct product = catalog.findByName("Test").iterator().next();
//        controller.addProductToCart(product, -100, "1", cart, session, user);  //must be 0
//        controller.addProductToCart(product, 100, "1", cart, session, user);   //must be 11
//        //long id= product.getProductNumber();
//        assertEquals(cart.iterator().next().getQuantity().getAmount().intValueExact(), 11);
//    }
//
//
//    @Test
//    public void buy() throws Exception {
//        Model model = new ExtendedModelMap();
//
//    }


    @After
       public void clearData() {
        cart.clear();
        catalog.deleteAll();
    }


}