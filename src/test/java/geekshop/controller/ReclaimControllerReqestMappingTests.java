package geekshop.controller;

import geekshop.AbstractWebIntegrationTests;
import geekshop.model.*;
import org.junit.Before;
import org.junit.Test;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.ProductIdentifier;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.order.OrderLine;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Midokin on 15.01.2015.
 */
public class ReclaimControllerReqestMappingTests extends AbstractWebIntegrationTests{

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

    private User hans;

    @Before
    public void setup() {
        hans = userRepo.findByUserAccount(uam.findByUsername("hans").get());

        login("owner", "123");

        super.setUp();
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
                        .with(user("hans").roles("EMPLOYEE"))
                        .param("searchordernumber", String.valueOf(orderNumber))
                        .sessionAttr("isReclaim", true))
                        .andReturn().getRequest().getSession().getAttribute("mapAmounts");

        Cart cart = (Cart) mvc.perform(post("/reclaimcart")
                .with(user("hans").roles("EMPLOYEE"))
                .param("orderNumber", String.valueOf(orderNumber))
                .param("rpid", reclProduct.getId().toString())
                .param("rnumber", "2")
                .sessionAttr("isReclaim", true)
                .sessionAttr("mapAmounts", mapAmounts))
                .andReturn().getModelAndView().getModel().get("cart");

        mvc.perform(post("/reclaimrequest")
                .with(user("hans").roles("EMPLOYEE"))
                .param("orderNumber", String.valueOf(orderNumber))
                .sessionAttr("cart", cart)
                .sessionAttr("overview", true)
                .sessionAttr("oN", String.valueOf(orderNumber)));

        assertEquals(orderRepo.count(), iterableCount + 1);
        assertEquals(productQuantity, inventory.findByProduct(reclProduct).get().getQuantity().getAmount().intValueExact());
    }

    @Test
    public void recConReclaim() throws Exception {
        mvc.perform(get("/reclaim")
                .with(user("hans").roles("EMPLOYEE")))
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
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        Map<GSProduct, BigDecimal> mapAmounts = (Map<GSProduct, BigDecimal>)
                mvc.perform(get("/ordersearch")
                        .with(user("hans").roles("EMPLOYEE"))
                        .param("searchordernumber", String.valueOf(orderNumber))
                        .sessionAttr("isReclaim", true))
                        .andReturn().getRequest().getSession().getAttribute("mapAmounts");

        mvc.perform(post("/reclaimcart")
                .with(user("hans").roles("EMPLOYEE"))
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

        mvc.perform(post("/reclaimcart")
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
                        .with(user("hans").roles("EMPLOYEE"))
                        .param("searchordernumber", String.valueOf(num))
                        .sessionAttr("isReclaim", true))
                        .andReturn().getRequest().getSession().getAttribute("mapAmounts");

        mvc.perform(post("/alltoreclaimcart")
                .with(user("hans").roles("EMPLOYEE"))
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
                .with(user("hans").roles("EMPLOYEE"))
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
                .with(user("hans").roles("EMPLOYEE"))
                .param("searchordernumber", searchOrderNumber)
                .sessionAttr("isReclaim", false))
                .andExpect(status().isOk())
                .andExpect(view().name("reclaim"))
                .andExpect(model().attributeExists("reclaimorder"))
                .andExpect(model().attributeExists("catalog"));

        mvc.perform(get("/ordersearch")
                .with(user("hans").roles("EMPLOYEE"))
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
                .with(user("hans").roles("EMPLOYEE"))
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
                        .with(user("hans").roles("EMPLOYEE"))
                        .param("searchordernumber", String.valueOf(orderNumber))
                        .sessionAttr("isReclaim", true))
                        .andReturn().getRequest().getSession().getAttribute("mapAmounts");

        Cart cart = (Cart) mvc.perform(post("/reclaimcart")
                .with(user("hans").roles("EMPLOYEE"))
                .param("orderNumber", String.valueOf(orderNumber))
                .param("rpid", reclProduct.getId().toString())
                .param("rnumber", "2")
                .sessionAttr("isReclaim", true)
                .sessionAttr("mapAmounts", mapAmounts))
                .andReturn().getModelAndView().getModel().get("cart");


        CartItem testCartItem = cart.iterator().next();

        mvc.perform(post("/updatereclaimcartitem/")
                .with(user("hans").roles("EMPLOYEE"))
                .param("identifier", testCartItem.getIdentifier())
                .param("quantity", quantity)
                .sessionAttr("oN", orderNumber)
                .sessionAttr("cart", cart))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/cart"));

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(post("/updatereclaimcartitem/")
                .with(user("hans").roles("EMPLOYEE"))
                .param("identifier", testCartItem.getIdentifier())
                .param("quantity", quantity))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void recConCancelReclaim() throws Exception {
        mvc.perform(get("/cancelreclaim")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/reclaim"));

        mvc.perform(post("/cancelreclaim")
                .with(user("hans").roles("EMPLOYEE")))
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
