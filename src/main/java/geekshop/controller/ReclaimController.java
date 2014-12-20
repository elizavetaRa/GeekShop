package geekshop.controller;

/*
 * Created by Basti on 20.11.2014.
 */

import geekshop.model.*;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.Product;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.order.Cart;
import org.salespointframework.payment.PaymentMethod;
import org.salespointframework.quantity.Units;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * A Spring MVC controller to manage the {@link org.salespointframework.order.Cart}.
 *
 * @author Sebastian D&ouml;ring
 */

@Controller
@PreAuthorize("isAuthenticated()")
@SessionAttributes("cart")
class ReclaimController {
    private PaymentMethod paymentMethod;
    private final Inventory<GSInventoryItem> inventory;
    private final BusinessTime businessTime;
    private final Catalog<GSProduct> catalog;
    private final UserRepository userRepo;
    private final GSOrderRepository orderRepo;


    /**
     * Creates a new {@link CartController} with the given {@link OrderManager}.
     *
     * @param orderManager must not be {@literal null}.
     */
    @Autowired
    public ReclaimController(Inventory<GSInventoryItem> inventory, BusinessTime businessTime, Catalog<GSProduct> catalog, UserRepository userRepo, GSOrderRepository orderRepo) {

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

    @ModelAttribute("cart")
    public Cart initializeReclaimCart() {
        return new Cart();
    }


    @RequestMapping("/reclaimcart")
    public String reclaimcart() {
        return "cart";
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
    public String addProductToReclaimCart(@RequestParam("rpid") Product product, @RequestParam("rnumber") long number, @ModelAttribute Cart cart) {

        if (number <= 0) {
            number = 1;
        }
        if (number > inventory.findByProduct(product).get().getQuantity().getAmount().intValueExact()) {
            number = inventory.findByProduct(product).get().getQuantity().getAmount().intValueExact();
        }


        cart.addOrUpdateItem(product, Units.of(number));
        return "redirect:/reclaim";

    }


    @RequestMapping(value = "/deleteallreclaimitems", method = RequestMethod.DELETE)
    public String deleteAll(@ModelAttribute Cart cart) {
        cart.clear();
        return "redirect:/cart";
    }

    @RequestMapping(value = "/deletereclaimitem/", method = RequestMethod.POST)
    public String deleteCartItem(@RequestParam String identifier, @ModelAttribute Cart cart) {
        cart.removeItem(identifier);
        return "redirect:/cart";
    }


    @RequestMapping(value = "/reclaimcart", method = RequestMethod.GET)
    public String reclaimbasket() {
        return "cart";
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


    @RequestMapping("/ordersearch")
    public String searchOrderByNumber(Model model, @RequestParam(value = "searchordernumber", required = true) String searchOrdernumber, @LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        int id = Integer.parseInt(searchOrdernumber);
        Optional<GSOrder> optOrder = orderRepo.findByOrderNumber(id);
        if (!optOrder.isPresent()) {
            System.out.println("Keine Rechung gefunden!");
        } else if (optOrder.get().getOrderType() == OrderType.RECLAIM) {
            System.out.println("Rechnung " + id + " ist schon eine Reklamation!");
        } else if (optOrder.get().isCompleted()) { // Es muss noch überprüft werden, ob es innerhalb der 14 Tage liegt!!! Wenn nicht, muss die Order completed werden.
            System.out.println("Rechnung " + id + " liegt nicht mehr innerhalb des 14-Tage-Fensters!");
        } else if (optOrder.get().isCanceled()) {
            System.out.println("Rechnung " + id + " wurde storniert!");
        } else {
            model.addAttribute("reclaimorder", optOrder.get());
        }

        return "reclaim";


//        int id = Integer.parseInt(searchOrdernumber);
//        System.out.println("Soll nach dieser NUmmer suchen:  " + id);
//        Iterator<GSOrder> allOrders = orderRepo.findAll().iterator();
//        while (allOrders.hasNext()) {                              //iteriert über alle Orders, sucht nach einer mit eingegebener searchOrdernumber
//            System.out.println("in der Schleife  ");
//            GSOrder tempOrder = allOrders.next();
//            System.out.println("Aktuelle OrderNumber "+ tempOrder.getOrderNumber());
//            if ((tempOrder.getOrderNumber() == id) && (!tempOrder.isCompleted()) && (!tempOrder.isCanceled()) ) {  //wenn gefunden, nicht schon reklamiert und nicht completed, setzt Attribut reclaimorder
//
//                model.addAttribute("reclaimorder", tempOrder);
//                System.out.println("Order gefunden:  " + tempOrder);
//                return "reclaim";
//            }
//        }


//        System.out.println("nichts gefunden");
//        return "reclaim";
    }


// public void reclaim(){
//            //orderline.state='reclaimed';
//  LocalDateTime timeup= time.plusDays(14);


//Interval interval=new Interval(time, timeup);
//        }

}