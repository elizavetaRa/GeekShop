package geekshop.controller;

/*
 * Created by Basti on 20.11.2014.
 */

import geekshop.model.*;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.Product;
import org.salespointframework.core.SalespointIdentifier;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.order.Cart;
import org.salespointframework.order.OrderManager;
import org.salespointframework.payment.Cash;
import org.salespointframework.payment.Cheque;
import org.salespointframework.payment.CreditCard;
import org.salespointframework.payment.PaymentMethod;
import org.salespointframework.quantity.Units;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

/**
 * A Spring MVC controller to manage the {@link org.salespointframework.order.Cart}.
 *
 * @author Sebastian D&ouml;ring
 */

@Controller
@PreAuthorize("isAuthenticated()")
@SessionAttributes("reclaimcart")

class ReclaimController {
    private PaymentMethod paymentMethod;
    private final OrderManager<GSOrder> orderManager;
    private final Inventory<GSInventoryItem> inventory;
    private final BusinessTime businessTime;
    private final Catalog<GSProduct> catalog;
    private final UserRepository userRepo;
    private final GSOrderRepository orderRepo;
    private GSOrder lastorder;


    /**
     * Creates a new {@link CartController} with the given {@link OrderManager}.
     *
     * @param orderManager must not be {@literal null}.
     */
    @Autowired
    public ReclaimController(OrderManager<GSOrder> orderManager, Inventory<GSInventoryItem> inventory, BusinessTime businessTime, Catalog<GSProduct> catalog, UserRepository userRepo, GSOrderRepository orderRepo) {

        Assert.notNull(orderManager, "OrderManager must not be null!");
        this.orderManager = orderManager;
        this.inventory = inventory;
        this.businessTime = businessTime;
        this.catalog = catalog;
        this.userRepo = userRepo;
        this.orderRepo = orderRepo;

    }


    @RequestMapping("/reclaim")
    public String reclaim() {
        return "reclaim";
    }

    @ModelAttribute("reclaimcart")
    public Cart initializeReclaimCart() {
        return new Cart();
    }



    @RequestMapping("/reclaimcart")
    public String reclaimcart() {
        return "reclaimcart";
    }

    /**
     * Adds a {@link Product} to the {@link Cart}. Note how the type of the parameter taking the request parameter
     * {@code pid} is {@link Product}. For all domain types extening {@link org.salespointframework.core.AbstractEntity} (directly or indirectly) a tiny
     * Salespoint extension will directly load the object instance from the database. If the identifier provided is
     * invalid (invalid format or no {@link Product} with the id found), {@literal null} will be handed into the method.
     *
     * @param product
     * @param number
     * @param model
     * @return
     */


    @RequestMapping(value = "/reclaimcart", method = RequestMethod.POST)
    public String addProductToReclaimCart(@RequestParam("rpid") Product product, @RequestParam("rnumber") long number, @ModelAttribute Cart reclaimcart) {

        if (number <= 0) {
            number = 1;
        }
        if (number > inventory.findByProduct(product).get().getQuantity().getAmount().intValueExact()) {
            number = inventory.findByProduct(product).get().getQuantity().getAmount().intValueExact();
        }


        reclaimcart.addOrUpdateItem(product, Units.of(number));
        return "redirect:/reclaim";

    }


    @RequestMapping(value = "/deleteallreclaimitems", method = RequestMethod.DELETE)
    public String deleteAll(@ModelAttribute Cart reclaimcart) {
        reclaimcart.clear();
        return "redirect:/reclaimcart";
    }

    @RequestMapping(value = "/deletereclaimitem/", method = RequestMethod.POST)
    public String deleteCartItem(@RequestParam String identifier, @ModelAttribute Cart reclaimcart) {
        reclaimcart.removeItem(identifier);
        return "redirect:/reclaimcart";
    }


    @RequestMapping(value = "/reclaimcart", method = RequestMethod.GET)
    public String reclaimbasket() {
        return "reclaimcart";
    }

//    @RequestMapping("/checkout")
//    public String checkout() {
//        return "checkout";
//    }




    /**
     * Checks out the current state of the {@link Cart}. Using a method parameter of type {@code Optional<UserAccount>}
     * annotated with {@link LoggedIn} you can access the {@link UserAccount} of the currently logged in user.
     *
     * @param userAccount must not be {@literal null}.
     * @return
     */

    @RequestMapping("/reclaimoverview")
    public String reclaimoverview() {
        return "reclaimoverview";
    }






    //   @RequestMapping(value = "/searchorder", method = RequestMethod.POST)

//    public GSOrder searchOrderByNumber(String orderNumber, Model model, @RequestParam(value = "searchordernumber", required = true) String searchOrdernumber)
//    {
//      //  Iterable<GSOrder> allOrders = orderRepo.findAll();
//
//        if (orderRepo.findByOrderNumber(new SalespointIdentifier(orderNumber))!= null) {
//
//
//        }
//        if (searchOrdernumber == null) {
//            return "redirect:/reclaim";
//        } else
//            model.addAttribute("catalog", sortProductByName(search(searchTerm), "asc"));
//        model.addAttribute("superCategories", supRepo.findAll());
//        model.addAttribute("subCategories", subRepo.findAll());
//
//
//        return "redirect:/reclaim";
//
//    }


}






// public void reclaim(){
//            //orderline.state='reclaimed';
//  LocalDateTime timeup= time.plusDays(14);


//Interval interval=new Interval(time, timeup);
//        }

