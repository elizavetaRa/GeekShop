package geekshop.controller;

/*
 * Created by Basti on 20.11.2014.
 */

import geekshop.model.*;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.Product;
import org.salespointframework.catalog.ProductIdentifier;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.order.OrderLine;
import org.salespointframework.payment.Cash;
import org.salespointframework.payment.PaymentMethod;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Iterator;
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
    private final MessageRepository messageRepo;


    /**
     * Creates a new {@link CartController} with the given {@link OrderManager}.
     *
     * @param orderManager must not be {@literal null}.
     */
    @Autowired
    public ReclaimController(Inventory<GSInventoryItem> inventory, BusinessTime businessTime, Catalog<GSProduct> catalog, UserRepository userRepo, GSOrderRepository orderRepo, MessageRepository messageRepo) {

        this.inventory = inventory;
        this.businessTime = businessTime;
        this.catalog = catalog;
        this.userRepo = userRepo;
        this.orderRepo = orderRepo;
        this.messageRepo=messageRepo;

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
    public String addProductToReclaimCart(@RequestParam("orderNumber") long num, @RequestParam("rpid") ProductIdentifier productid,
                                          @RequestParam("rnumber") int reclaimnumber,
                                          @ModelAttribute Cart cart, HttpSession session, Model model, @LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        if (!((boolean) session.getAttribute("isReclaim")))
            session.setAttribute("isReclaim", true);
        model.addAttribute("reclaimorder", orderRepo.findByOrderNumber(num).get());

        for (OrderLine line : orderRepo.findByOrderNumber(num).get().getOrderLines()) {
            if (line.getProductIdentifier().equals(productid)) {
                System.out.println("richtige Orderline gefunden  " + line.toString());
                if (reclaimnumber > line.getQuantity().getAmount().intValueExact()) {
                    reclaimnumber = line.getQuantity().getAmount().intValueExact();
                }
                if (reclaimnumber <= 0) {
                    return "redirect:/reclaim";
                }
                Quantity qnumber = new Quantity(reclaimnumber, line.getQuantity().getMetric(), line.getQuantity().getRoundingStrategy());
                cart.addOrUpdateItem(catalog.findOne(line.getProductIdentifier()).get(), qnumber);
                model.addAttribute("orderNumber", num);
                session.setAttribute("oN", num);

                return "redirect:/reclaim";
            }
        }
        return "redirect:/reclaim";

    }

    @RequestMapping(value = "/alltoreclaimcart", method = RequestMethod.POST)
    public String allToReclaimCart(@RequestParam("orderNumber") long num, @ModelAttribute Cart cart, Model model, HttpSession session,@LoggedIn Optional<UserAccount> userAccount) {

        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        OrderLine line;

        for (Iterator<OrderLine> iterator = orderRepo.findByOrderNumber(num).get().getOrderLines().iterator();
             iterator.hasNext(); ) {
            line = iterator.next();
            cart.addOrUpdateItem(catalog.findOne(line.getProductIdentifier()).get(), line.getQuantity());

        }

        model.addAttribute("orderNumber", num);
        session.setAttribute("oN", num);
        return "cart";
    }


    @RequestMapping(value = "/reclaimrequest", method = RequestMethod.POST)
    public String reclaimIt(@ModelAttribute Cart cart, @RequestParam("orderNumber") String strNum, HttpSession session, @LoggedIn final Optional<UserAccount> userAccount, Model model) {

        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        return userAccount.map(account -> {

            if (((boolean) session.getAttribute("overview")))
                session.setAttribute("overview", false);

            System.out.println("strNum:  "+strNum);
            long num= Long.parseLong(strNum);
            GSOrder reclaimorder = new GSOrder(userAccount.get(), new Cash(), orderRepo.findByOrderNumber(num).get());

            // eigentlich cart.addItemsTo(order); Wir brauchen aber GSOrderLines!

            for (Iterator<CartItem> iterator = cart.iterator(); iterator.hasNext(); ) {
                CartItem cartItem = iterator.next();
                reclaimorder.add(new GSOrderLine(cartItem.getProduct(), cartItem.getQuantity()));
            }

            reclaimorder.pay();
            reclaimorder.setOrderType(OrderType.RECLAIM);
            orderRepo.save(reclaimorder);
            if (reclaimorder.isOpen()){
                System.out.println("reclaimoder is open");
            }

            String messageText = "Es wurden Produkte der Rechnung " + GSOrder.longToString(/*reclaimorder*/orderRepo.findByOrderNumber(num).get().getOrderNumber()) + " zurückgegeben.";
            messageRepo.save(new Message(MessageKind.RECLAIM, messageText, reclaimorder));

            cart.clear();
            model.addAttribute("order", reclaimorder);
            return "orderoverview";
        }).orElse("orderoverview");
    }


    @RequestMapping(value = "/reclaimcart", method = RequestMethod.GET)
    public String reclaimbasket() {
        return "cart";
    }

    @RequestMapping("/ordersearch")
    public String searchOrderByNumber(Model model, @RequestParam(value = "searchordernumber", required = true) String searchOrderNumber, HttpSession session, @LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        if (!((boolean) session.getAttribute("isReclaim")))
            session.setAttribute("isReclaim", true);
        
        long oNumber = Long.parseLong(searchOrderNumber);
        System.out.println(oNumber + " orderNumber");
        // int id = Integer.parseInt(searchOrdernumber);
        Optional<GSOrder> optOrder = orderRepo.findByOrderNumber(oNumber);
        if (!optOrder.isPresent()) {
            System.out.println("Keine Rechnung gefunden!");
            String noOrder= "";
            model.addAttribute("noorder", noOrder);
        } else if (optOrder.get().getOrderType() == OrderType.RECLAIM) {
            System.out.println("Rechnung " + oNumber + " ist schon eine Reklamation!");
            String alreadyReclaim= "";
            model.addAttribute("alreadyreclaim", alreadyReclaim);
        } else if (optOrder.get().isCompleted()) { // Es muss noch überprüft werden, ob es innerhalb der 14 Tage liegt!!! Wenn nicht, muss die Order completed werden.
            System.out.println("Rechnung " + oNumber + " liegt nicht mehr innerhalb des 14-Tage-Fensters!");
            String toolate= "";
            model.addAttribute("toolate", toolate);
        } else if (optOrder.get().isCanceled()) {
            System.out.println("Rechnung " + oNumber + " wurde storniert!");
            String canceled= "";
            model.addAttribute("canceled", canceled);
        } else {
            model.addAttribute("furtherreclaim",true);
            model.addAttribute("reclaimorder", optOrder.get());
        }

        return "reclaim";
    }

    @RequestMapping("/rcheckout")
    public String checkout(@LoggedIn Optional<UserAccount> userAccount, @RequestParam("orderNumber") String strNumber, Model model) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        long orderNumber= Long.parseLong(strNumber);
        model.addAttribute("orderNumber", orderNumber);

        return "checkout";
    }


    @RequestMapping(value = "/updatereclaimcartitem/", method = RequestMethod.POST)
    public String updateReclaimCartItem(@RequestParam String identifier, @RequestParam String quantity, HttpSession session, /*@RequestParam ("orderNumber") String strNumber,*/ @ModelAttribute Cart cart, Model model, @LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        int oldquantity = Integer.parseInt(cart.getItem(identifier).get().getQuantity().getAmount().toString());
        int newquantity = Integer.parseInt(quantity);
        if (newquantity <= 0) {
            newquantity = 0;
        }
        int updatequantity = newquantity - oldquantity;

        long num= (long)session.getAttribute("oN");
        OrderLine line;
        int inOrder = 0;
        for (Iterator<OrderLine> iterator = orderRepo.findByOrderNumber(num).get().getOrderLines().iterator();
             iterator.hasNext(); ) {
            line = iterator.next();                                                         //search Product of cartItem in order to compare quantity
            if (line.getProductName() == cart.getItem(identifier).get().getProductName()) {
                inOrder = line.getQuantity().getAmount().intValueExact();
                System.out.println("In Order: " + inOrder);
                break;
            }
        }

        if (inOrder <= newquantity) {
            updatequantity = inOrder - oldquantity;
        }
        session.setAttribute("oN", num);
        cart.addOrUpdateItem(cart.getItem(identifier).get().getProduct(), new Quantity(updatequantity, cart.getItem(identifier).get().getQuantity().getMetric(), cart.getItem(identifier).get().getQuantity().getRoundingStrategy()));
        return "redirect:/cart";
    }

}