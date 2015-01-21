package geekshop.controller;


import geekshop.model.*;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.ProductIdentifier;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.order.OrderLine;
import org.salespointframework.payment.Cash;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * A Spring MVC controller to manage the {@link org.salespointframework.order.Cart}.
 *
 * @author Elizaveta Ragozina
 * @author Sebastian Döring
 */

@Controller
@PreAuthorize("isAuthenticated()")
@SessionAttributes("cart")
class ReclaimController {
    private final Catalog<GSProduct> catalog;
    private final GSOrderRepository orderRepo;
    private final MessageRepository messageRepo;


    /**
     * Creates a new {@link ReclaimController} with the given {@link Catalog}.
     *
     * @param catalog     must not be {@literal null}.
     * @param orderRepo   must not be {@literal null}.
     * @param messageRepo must not be {@literal null}.
     */
    @Autowired
    public ReclaimController(Catalog<GSProduct> catalog, GSOrderRepository orderRepo, MessageRepository messageRepo) {
        Assert.notNull(catalog, "catalog must not be null!");
        Assert.notNull(messageRepo, "messageRepo must not be null!");
        Assert.notNull(orderRepo, "orderRepo must not be null!");
        this.catalog = catalog;
        this.orderRepo = orderRepo;
        this.messageRepo = messageRepo;

    }

    /**
     * Returns the view of reclaim.
     */
    @RequestMapping("/reclaim")
    public String reclaim(Model model, @LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        model.addAttribute("catalog", catalog);
        return "reclaim";
    }


    /**
     * Creates a new {@link Cart} instance to be stored in the session (see the class-level {@link SessionAttributes}
     * annotation).
     *
     * @return a new {@link Cart} instance.
     */
    @ModelAttribute("cart")
    public Cart initializeReclaimCart() {
        return new Cart();
    }

    /**
     * Returns the view of reclaimcart.
     */
    @RequestMapping("/reclaimcart")
    public String reclaimcart(@LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        return "cart";
    }

    /**
     * Adds a quantity to relcaim of {@link geekshop.model.GSProduct} from {@link GSOrderLine} of reclaiming
     * {@link GSOrder} to the {@link Cart}.
     *
     * @param num           must not be {@literal null}.
     * @param productid     must not be {@literal null}.
     * @param reclaimnumber must not be {@literal null}.
     * @param session       must not be {@literal null}.
     * @param userAccount   must not be {@literal null}.
     * @param model
     */
    @RequestMapping(value = "/reclaimcart", method = RequestMethod.POST)
    public String addProductToReclaimCart(@RequestParam("orderNumber") long num, @RequestParam("rpid") ProductIdentifier productid,
                                          @RequestParam("rnumber") int reclaimnumber, @ModelAttribute Cart cart, HttpSession session,
                                          Model model, @LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        if (!((boolean) session.getAttribute("isReclaim")))
            session.setAttribute("isReclaim", true);

        //searchs for reclaimedOrder by the  orderNumber
        GSOrder reclaimedOrder = orderRepo.findByOrderNumber(num).get();

        model.addAttribute("reclaimorder", reclaimedOrder);
        session.setAttribute("ro", reclaimedOrder);

        if (reclaimnumber <= 0) {
            return "redirect:/reclaim";
        }

        //puts chosen product from orderLine to the cart
        GSOrderLine reclaimedOL = (GSOrderLine) reclaimedOrder.findOrderLineByProduct(catalog.findOne(productid).get());

        if (reclaimedOL == null)
            return "redirect:/reclaim";

        BigDecimal maxAmount = ((Map<GSProduct, BigDecimal>) session.getAttribute("mapAmounts")).get(catalog.findOne(reclaimedOL.getProductIdentifier()).get());
        if (reclaimnumber > maxAmount.intValue())
            reclaimnumber = maxAmount.intValue();

        CartItem cartItem = null;
        for (CartItem ci : cart) {
            if (ci.getProduct().getIdentifier().equals(reclaimedOL.getProductIdentifier())) {
                cartItem = ci;
                break;
            }
        }

        if (cartItem != null && cartItem.getQuantity().getAmount().intValue() + reclaimnumber > maxAmount.intValue()) {
            reclaimnumber = maxAmount.intValue() - cartItem.getQuantity().getAmount().intValue();
        }

        Quantity qnumber = new Quantity(reclaimnumber, reclaimedOL.getQuantity().getMetric(), reclaimedOL.getQuantity().getRoundingStrategy());
        cart.addOrUpdateItem(catalog.findOne(productid).get(), qnumber);

        model.addAttribute("orderNumber", num);
        session.setAttribute("oN", num);        //picks up ordernumber for next steps

        return "redirect:/reclaim";
    }

    /**
     * Adds a all products  of reclaiming {@link GSOrder} to the {@link Cart}.
     *
     * @param num         must not be {@literal null}.
     * @param session     must not be {@literal null}.
     * @param userAccount must not be {@literal null}.
     * @param model
     */
    @RequestMapping(value = "/alltoreclaimcart", method = RequestMethod.POST)
    public String allToReclaimCart(@RequestParam("orderNumber") long num, @ModelAttribute Cart cart, Model model, HttpSession session, @LoggedIn Optional<UserAccount> userAccount) {

        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        cart.clear(); // to avoid false quantity

        for (OrderLine orderLine : orderRepo.findByOrderNumber(num).get().getOrderLines()) {
            BigDecimal maxAmount = ((Map<GSProduct, BigDecimal>) session.getAttribute("mapAmounts")).get(catalog.findOne(orderLine.getProductIdentifier()).get());
            if (maxAmount.signum() > 0) {
                cart.addOrUpdateItem(catalog.findOne(orderLine.getProductIdentifier()).get(),
                        new Quantity(maxAmount, orderLine.getQuantity().getMetric(), orderLine.getQuantity().getRoundingStrategy()));
            }
        }

        model.addAttribute("orderNumber", num);
        session.setAttribute("oN", num);
        return "cart";
    }

    /**
     * Creates reclaimed Order out of the content of {@link Cart}.
     *
     * @param strNum
     * @param session     must not be {@literal null}.
     * @param userAccount must not be {@literal null}.
     * @param model
     */
    @RequestMapping(value = "/reclaimrequest", method = RequestMethod.POST)
    public String reclaimIt(@ModelAttribute Cart cart, @RequestParam("orderNumber") String strNum, HttpSession session, @LoggedIn final Optional<UserAccount> userAccount, Model model) {

        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        return userAccount.map(account -> {

            if (((boolean) session.getAttribute("overview")))
                session.setAttribute("overview", false);

            long num = Long.parseLong(strNum);
            GSOrder reclaimorder = new GSOrder(userAccount.get(), new Cash(), orderRepo.findByOrderNumber(num).get());


            for (CartItem cartItem : cart) {
                reclaimorder.add(new GSOrderLine(cartItem.getProduct(), cartItem.getQuantity()));
            }

//            reclaimorder.pay();
//            reclaimorder.setOrderType(OrderType.RECLAIM);
            orderRepo.save(reclaimorder);

            //creates message with information of reclaimOrder
            String messageText = "Es wurden Produkte der Rechnung " + GSOrder.longToString(reclaimorder.getReclaimedOrder().getOrderNumber()) + " zurückgegeben.";
            messageRepo.save(new Message(MessageKind.RECLAIM, messageText, reclaimorder));

            cart.clear();
            model.addAttribute("order", reclaimorder);
            model.addAttribute("catalog", catalog);
            session.removeAttribute("oN");
            session.removeAttribute("ro");
            return "orderoverview";
        }).orElse("orderoverview");
    }

    /**
     * Returns current state of {@link Cart} in reclaiming modus.
     */
    @RequestMapping(value = "/reclaimcart", method = RequestMethod.GET)
    public String reclaimbasket(@LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        return "cart";
    }

    /**
     * Searchs order by given orderNumber out of the content of {@link Cart}.
     *
     * @param searchOrderNumber must not be {@literal null}.
     * @param session           must not be {@literal null}.
     * @param userAccount       must not be {@literal null}.
     * @param model
     */
    @RequestMapping("/ordersearch")
    public String searchOrderByNumber(Model model, @RequestParam(value = "searchordernumber", required = true) String searchOrderNumber, HttpSession session, @LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        if (!((boolean) session.getAttribute("isReclaim")))
            session.setAttribute("isReclaim", true);

        if (searchOrderNumber == null || searchOrderNumber.trim().isEmpty() || !searchOrderNumber.trim().matches("\\d+")) {
            String error = "Eingabe muss eine Rechnungsnummer sein.";
            model.addAttribute("error", error);
            return "reclaim";
        }

        long oNumber = Long.parseLong(searchOrderNumber.trim());
        String orderNumber = GSOrder.longToString(oNumber);

        Optional<GSOrder> optOrder = orderRepo.findByOrderNumber(oNumber);
        if (!optOrder.isPresent()) {
            String error = "Rechnung " + orderNumber + " nicht gefunden!";
            model.addAttribute("error", error);
        } else if (optOrder.get().getOrderType() == OrderType.RECLAIM) {
            String error = "Rechnung " + orderNumber + " ist schon eine Reklamation!";
            model.addAttribute("error", error);
        } else if (optOrder.get().isCompleted()) {
            String error = "Rechnung " + orderNumber + " liegt nicht mehr innerhalb des 14-Tage-Fensters!";
            model.addAttribute("error", error);
        } else if (optOrder.get().isCanceled()) {
            String error = "Rechnung " + orderNumber + " wurde storniert!";
            model.addAttribute("error", error);
        } else {
            model.addAttribute("reclaimorder", optOrder.get());
            model.addAttribute("catalog", catalog);
            session.setAttribute("ro", optOrder.get());

            Map<GSProduct, BigDecimal> mapAmounts = new HashMap<>();
            for (OrderLine ol : optOrder.get().getOrderLines()) {
                GSProduct product = catalog.findOne(ol.getProductIdentifier()).get();
                mapAmounts.put(product, determineNotReclaimedAmountOfProduct(optOrder.get(), product));
            }
            session.setAttribute("mapAmounts", mapAmounts);
        }

        return "reclaim";
    }

    /**
     * Returns current view of checkout in reclaiming modus.
     */
    @RequestMapping("/rcheckout")
    public String checkout(@LoggedIn Optional<UserAccount> userAccount, @RequestParam("orderNumber") String strNumber, Model model) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        long orderNumber = Long.parseLong(strNumber);
        model.addAttribute("orderNumber", orderNumber);

        return "checkout";
    }

    /**
     * Updates  {@link Cart} in reclaiming modus with given {@link Quantity}.
     *
     * @param identifier
     * @param quantity
     * @param session     must not be {@literal null}.
     * @param userAccount must not be {@literal null}.
     */
    @RequestMapping(value = "/updatereclaimcartitem/", method = RequestMethod.POST)
    public String updateReclaimCartItem(@RequestParam String identifier, @RequestParam String quantity, HttpSession session, @ModelAttribute Cart cart, @LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        //checks if quantity for updating is allowed
        int oldquantity = Integer.parseInt(cart.getItem(identifier).get().getQuantity().getAmount().toString());
        int newquantity = Integer.parseInt(quantity);
        if (newquantity <= 0) {
            newquantity = 0;
        }
        int updatequantity = newquantity - oldquantity;

        long num = (long) session.getAttribute("oN");
        OrderLine line;
        int inOrder = 0;
        for (Iterator<OrderLine> iterator = orderRepo.findByOrderNumber(num).get().getOrderLines().iterator();
             iterator.hasNext(); ) {
            line = iterator.next();        //search Product of cartItem in order to compare quantity
            if (line.getProductName() == cart.getItem(identifier).get().getProductName()) {
                inOrder = line.getQuantity().getAmount().intValueExact();
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

    /**
     * Cancels current reclaim.
     *
     * @param session     must not be {@literal null}.
     * @param userAccount must not be {@literal null}.
     */
    @RequestMapping(value = "/cancelreclaim", method = RequestMethod.POST)
    public String cancelReclaim(@LoggedIn Optional<UserAccount> userAccount, HttpSession session, @ModelAttribute Cart cart) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        session.removeAttribute("ro");
        cart.clear();
        return "redirect:/reclaim";
    }

    /**
     * Returns current view after canceling reclaim.
     *
     * @param userAccount must not be {@literal null}.
     */
    @RequestMapping("/cancelreclaim")
    public String cancelReclaim(@LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        return "redirect:/reclaim";
    }

    /**
     * As the method name says, determines the not reclaimed amount of {@link GSProduct} bought in the given order.
     *
     * @param orderToBeReclaimed the sale which contains the given product
     * @param product the product whose not reclaimed amount is to be determined
     */
    private BigDecimal determineNotReclaimedAmountOfProduct(GSOrder orderToBeReclaimed, GSProduct product) {
        if (orderToBeReclaimed.getOrderType() == OrderType.RECLAIM)
            throw new IllegalArgumentException("The given order is a reclaim order! Relaim orders cannot be reclaimed.");

        Iterable<GSOrder> reclaimOrders = orderRepo.findByReclaimedOrder(orderToBeReclaimed);
        BigDecimal cnt = orderToBeReclaimed.findOrderLineByProduct(product).getQuantity().getAmount();
        for (GSOrder order : reclaimOrders) {
            OrderLine ol = order.findOrderLineByProduct(product);
            if (ol != null) {
                cnt = cnt.subtract(ol.getQuantity().getAmount());
            }
        }
        return cnt;
    }
}