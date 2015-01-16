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

public class ReclaimControllerTests extends AbstractWebIntegrationTests {

    @Autowired
    ReclaimController controller;
    @Autowired
    CartController cartController;
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


    Cart cart;
    Model model;
    UserAccount userAccount;


    @Before
    public void setUp() {
        login("owner", "123");
        userAccount= userRepo.findAll().iterator().next().getUserAccount();
        cart = controller.initializeReclaimCart();
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


        session.setAttribute("isReclaim", true);

    }

    //most methods same as in CartController

    //tests if  order is correctly added to cart
    @Test
    public void testaddOrderToCart() throws Exception {

        gsOrderRepository.deleteAll();
        GSProduct product = catalog.findByName("test1").iterator().next();
        cartController.addProductToCart(product, 5, "1", cart, session, Optional.of(userAccount), model);
        cartController.buy(cart, session, "CASH", Optional.of(userAccount),model);
        assertTrue(gsOrderRepository.findAll().iterator().hasNext());
        GSOrder order= gsOrderRepository.findAll().iterator().next();
        boolean got=true;
        if (order==null) got=false;
        assertTrue(got);
        cart.clear();
    }

    //tests creating of reclaimOrder
    @Test
    public void testreclaimIt() throws Exception {

        gsOrderRepository.deleteAll();
        session.setAttribute("isReclaim", false);
        GSProduct product = catalog.findByName("test1").iterator().next();
        cartController.addProductToCart(product, 5, "1", cart, session, Optional.of(userAccount), model);
        cartController.buy(cart, session, "CASH", Optional.of(userAccount),model);
        session.setAttribute("isReclaim", true);

        GSOrder order= gsOrderRepository.findAll().iterator().next();
        controller.reclaimIt(cart, String.valueOf(order.getOrderNumber()),session,Optional.of(userAccount),model);

        GSOrder recl= gsOrderRepository.findByOrderNumber(6).get();
        boolean rightorder=true;
        if (recl==null) rightorder=false;
       assertTrue(rightorder);
        cart.clear();
    }


    @After
    public void clearData() {
        cart.clear();
        catalog.deleteAll();
        gsOrderRepository.deleteAll();
    }


}