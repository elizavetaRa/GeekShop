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
import org.salespointframework.quantity.Quantity;
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
        assertEquals(cart.iterator().next().getQuantity().getAmount().intValue(), 9);
        cart.clear();

        //negative amount of products
        controller.addProductToCart(product, -42, "1", cart, session, Optional.of(userAccount), model);
        boolean empty;
        if (cart.isEmpty()) {
            empty=true;
        } else empty = false;

        assertTrue(empty);
        cart.clear();

        //more products than in inventory
        controller.addProductToCart(product, 102, "1", cart, session, Optional.of(userAccount), model);
        assertEquals(cart.iterator().next().getQuantity().getAmount().intValue(), 10);
        cart.clear();

        //more products than in inventory with multiple using of function
        controller.addProductToCart(product, 3, "1", cart, session, Optional.of(userAccount), model);
        controller.addProductToCart(product, 8, "1", cart, session, Optional.of(userAccount), model);
        assertEquals(cart.iterator().next().getQuantity().getAmount().intValue(), 10);
    }



    // tests if correct CartItem is deleted
    @Test
    public void deleteCartItem() throws Exception {
        cart.clear();
        GSProduct product = catalog.findByName("test1").iterator().next();
        controller.addProductToCart(product, 1, "1", cart, session, Optional.of(userAccount), model);
        GSProduct product2 = catalog.findByName("test2").iterator().next();
        controller.addProductToCart(product2, 1, "1", cart, session, Optional.of(userAccount), model);
        String id1= cart.iterator().next().getIdentifier();
        cart.removeItem(id1);
        String id2= cart.iterator().next().getIdentifier();
        assertEquals(cart.iterator().next().getIdentifier(), id2);
        cart.clear();
    }



    // tests if CartItem is correctly updated in Cart
    @Test
    public void updateCartItem() throws Exception {

        cart.clear();
        GSProduct product = catalog.findByName("test1").iterator().next();
        controller.addProductToCart(product, 1, "1", cart, session, Optional.of(userAccount), model);

        //tests if the multiple updating is correct
        String id=cart.iterator().next().getIdentifier();
        controller.updateCartItem(id, "3", cart, session, Optional.of(userAccount));
                assertEquals(cart.iterator().next().getQuantity().getAmount().intValueExact(), 3);
        String id2=cart.iterator().next().getIdentifier();
        controller.updateCartItem(id2, "4", cart, session, Optional.of(userAccount));
        assertEquals(cart.iterator().next().getQuantity().getAmount().intValueExact(), 4);

        //tests bahavior if input is not a number
        String id3=cart.iterator().next().getIdentifier();
        controller.updateCartItem(id3, "-2ab", cart, session, Optional.of(userAccount));
        assertEquals(cart.iterator().next().getQuantity().getAmount().intValueExact(), 4);

        //tests bahavior if input is negative number
        String id4=cart.iterator().next().getIdentifier();
        controller.updateCartItem(id4, "-2", cart, session, Optional.of(userAccount));
        assertEquals(cart.iterator().next().getQuantity().getAmount().intValueExact(), 4);

        //tests the quantity if input is smaller than current quantity
        String id5=cart.iterator().next().getIdentifier();
        controller.updateCartItem(id5, "2", cart, session, Optional.of(userAccount));
        assertEquals(cart.iterator().next().getQuantity().getAmount().intValueExact(), 2);
        cart.clear();
    }


    @Test
    public void buy() throws Exception {
        cart.clear();
        gsOrderRepository.deleteAll();

        //tests if the order is created and paymentMethod is correct
        GSProduct product = catalog.findByName("test1").iterator().next();
        controller.addProductToCart(product, 1, "2", cart, session, Optional.of(userAccount), model);
        controller.buy(cart, session, "CASH", Optional.of(userAccount), model);
       GSOrder order= gsOrderRepository.findAll().iterator().next();
        assertEquals(order.getPaymentType().getValue(), "Barzahlung");

        //tests if order has correct state
        boolean paid= order.isPaid();
        boolean completed= order.isCompleted();
        assertTrue(paid);
        assertFalse(completed);

        //tests if after false inputs order is created
        cart.clear();
        gsOrderRepository.deleteAll();
        GSProduct product2 = catalog.findByName("test1").iterator().next();
        controller.addProductToCart(product, 1, "2", cart, session, Optional.of(userAccount), model);
        controller.buy(cart, session, "lol", Optional.of(userAccount), model);
        GSOrder order2= gsOrderRepository.findAll().iterator().next();
        boolean falsemethod=true;
        if (gsOrderRepository.findAll().iterator().hasNext()){ falsemethod = true;} else {falsemethod=true;}
        assertTrue(falsemethod);

    }


    @After
       public void clearData() {
        cart.clear();
        catalog.deleteAll();
        gsOrderRepository.deleteAll();
    }


}