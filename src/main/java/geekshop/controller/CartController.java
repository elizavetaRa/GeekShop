package geekshop.controller;

/*
 * Created by Basti on 20.11.2014.
 */

import geekshop.model.GSInventoryItem;
import geekshop.model.GSOrder;
import geekshop.model.GSProduct;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.Cart;
import org.salespointframework.order.OrderManager;
import org.salespointframework.payment.Cash;
import org.salespointframework.payment.PaymentMethod;
import org.salespointframework.quantity.Units;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.inventory.InventoryItem;

import java.util.Optional;

/**
 * A Spring MVC controller to manage the {@link org.salespointframework.order.Cart}.
 *
 * @author Sebastian D&ouml;ring
 */

@Controller
@PreAuthorize("isAuthenticated()")
@SessionAttributes("cart")
class CartController {

    @RequestMapping("/cart")
    public String cart() {
        return "cart";
    }

    @RequestMapping("/reclaim")
    public String reclaim() {
        return "reclaim";
    }

    private PaymentMethod paymentMethod;
    private final OrderManager<GSOrder> orderManager;
    private final Inventory<GSInventoryItem> inventory;
    private final BusinessTime businessTime;

    /**
     * Creates a new {@link CartController} with the given {@link OrderManager}.
     *
     * @param orderManager must not be {@literal null}.
     */
    @Autowired
    public CartController(OrderManager<GSOrder> orderManager, Inventory<GSInventoryItem> inventory, BusinessTime businessTime) {

        Assert.notNull(orderManager, "OrderManager must not be null!");
        this.orderManager = orderManager;
        this.inventory = inventory;
        this.businessTime= businessTime;
    }

    /**
     * Creates a new {@link Cart} instance to be stored in the session (see the class-level {@link SessionAttributes}
     * annotation).
     *
     * @return a new {@link Cart} instance.
     */
    @ModelAttribute("cart")
    public Cart initializeCart() {
        return new Cart();
    }

    /**
     * Adds a {@link Product} to the {@link Cart}. Note how the type of the parameter taking the request parameter
     * {@code pid} is {@link Product}. For all domain types extening {@link AbstractEntity} (directly or indirectly) a tiny
     * Salespoint extension will directly load the object instance from the database. If the identifier provided is
     * invalid (invalid format or no {@link Product} with the id found), {@literal null} will be handed into the method.
     *
     * @param product
     * @param number
     * @param session
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/cart", method = RequestMethod.POST)

    public String addProductToCart(@RequestParam("pid") Product product, @RequestParam("number") long number, @ModelAttribute Cart cart,
                             ModelMap modelMap) {


        if (number <= 0){number =1;}
        if (number > inventory.findByProduct(product).get().getQuantity().getAmount().intValueExact())
        {number=inventory.findByProduct(product).get().getQuantity().getAmount().intValueExact();};

        cart.addOrUpdateItem(product,Units.of(number));
        return "redirect:/catalog";

    }

    @RequestMapping(value = "/cart", method = RequestMethod.GET)
    public String basket() {
        return "cart";
    }

    @RequestMapping("/checkout")
    public String checkout() {
        return "checkout";
    }






    /**
     * Checks out the current state of the {@link Cart}. Using a method parameter of type {@code Optional<UserAccount>}
     * annotated with {@link LoggedIn} you can access the {@link UserAccount} of the currently logged in user.
     *
     * @param session     must not be {@literal null}.
     * @param userAccount must not be {@literal null}.
     * @return
     */
          public String buy(@ModelAttribute Cart cart, @LoggedIn final Optional<UserAccount> userAccount) {

//                return userAccount.map(account -> {
//
//                    // (｡◕‿◕｡)
//                    // Mit commit wird der Warenkorb in die Order überführt, diese wird dann bezahlt und abgeschlossen.
//                    // Orders können nur abgeschlossen werden, wenn diese vorher bezahlt wurden.
//                    Order order = new GSOrder(userAccount, Cash.CASH);
//
//                    cart.addItemsTo(GSOrder);
//
//                    orderManager.payOrder(GSOrder);
//                    //  orderManager.completeOrder(GSOrder);
//                    orderManager.save(GSOrder);
//
//                    cart.clear();
//
//                    return "redirect:/";
//                }).orElse("redirect:/cart");

              return "cart";
            }

//        public void acceptReclaim(){
//            //orderline.state='reclaimed';
//        }

    }