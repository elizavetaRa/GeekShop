package geekshop.controller;

/*
 * Created by Basti on 20.11.2014.
 */

import geekshop.model.*;
import org.joda.money.CurrencyUnit;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.Product;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.payment.Cash;
import org.salespointframework.payment.Cheque;
import org.salespointframework.payment.CreditCard;
import org.salespointframework.payment.PaymentMethod;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.quantity.Units;
import org.salespointframework.time.BusinessTime;
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
import java.time.LocalDateTime;
import java.util.HashMap;
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
class CartController {
    private final Inventory<GSInventoryItem> inventory;
    private final BusinessTime businessTime;
    private final Catalog<GSProduct> catalog;
    private final UserRepository userRepo;
    private final GSOrderRepository orderRepo;


    /**
     * Creates a new {@link CartController} with the given {@link Inventory, @link BusinessTime, @link Catalog, @link UserRepository, @link GSOrderRepository}.
     *
     * @param inventory    must not be {@literal null}.
     * @param businessTime must not be {@literal null}.
     * @param catalog      must not be {@literal null}.
     * @param userRepo     must not be {@literal null}.
     * @param orderRepo    must not be {@literal null}.
     */
    @Autowired
    public CartController(Inventory<GSInventoryItem> inventory, BusinessTime businessTime, Catalog<GSProduct> catalog, UserRepository userRepo, GSOrderRepository orderRepo) {

        Assert.notNull(inventory, "inventory must not be null!");
        Assert.notNull(businessTime, "businessTime must not be null!");
        Assert.notNull(catalog, "catalog must not be null!");
        Assert.notNull(userRepo, "userRepo must not be null!");
        Assert.notNull(orderRepo, "orderRepo must not be null!");
        this.inventory = inventory;
        this.businessTime = businessTime;
        this.catalog = catalog;
        this.userRepo = userRepo;
        this.orderRepo = orderRepo;

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
     * Returns current state of {@link Cart}.
     */
    @RequestMapping("/cart")
    public String cart(Model model, @ModelAttribute Cart cart, HttpSession session, @LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        if (!(boolean) session.getAttribute("isReclaim")) {
            Iterable<CartItem> items = cart;
            for (CartItem ci : items) {
                if (!catalog.findOne(ci.getProduct().getId()).get().isInRange()) {
                    cart.removeItem(ci.getIdentifier());
                }
            }
        }

        model.addAttribute("inventory", inventory);

        return "cart";
    }


    /**
     * Adds a {@link Product} to the {@link Cart}. If the identifier provided is
     * invalid (invalid format or no {@link Product} with the id found), {@literal null} will be handed into the method.
     *
     * @param product
     * @param number
     * @param session
     * @param model
     */
    @RequestMapping(value = "/cart", method = RequestMethod.POST)
    public String addProductToCart(@RequestParam("pid") Product product, @RequestParam("number") long number, @RequestParam("query") String query,
                                   @ModelAttribute Cart cart, HttpSession session, @LoggedIn Optional<UserAccount> userAccount, Model model) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        if ((boolean) session.getAttribute("isReclaim"))
            session.setAttribute("isReclaim", false);
        //checks if number of products is given in correct way
        if (number <= 0) {
            return "redirect:/productsearch";
        }
        //if number is bigger then number of products in inventoryItem, puts the number of products in inventory
        if (number > inventory.findByProduct(product).get().getQuantity().getAmount().intValueExact()) {
            number = inventory.findByProduct(product).get().getQuantity().getAmount().intValueExact();
        }

        System.out.println("Anzahl Produkte vor dem hinzufügen zu Cart  " + inventory.findByProduct(product).get().getQuantity().getAmount().intValueExact());
        CartItem item = cart.addOrUpdateItem(product, Units.of(number));
        System.out.println("zu Cart hinzugefügt:  " + number);

        if (item.getQuantity().getAmount().intValueExact() >= inventory.findByProduct(product).get().getQuantity().getAmount().intValueExact()) {
            cart.removeItem(item.getIdentifier());
            cart.addOrUpdateItem(product, inventory.findByProduct(product).get().getQuantity());
        }

        for (Map.Entry<String, String> entry : getQueryMap(query).entrySet()) {
            model.addAttribute(entry.getKey(), entry.getValue());
        }

        return "redirect:/productsearch";

    }

    /**
     * Creates a map of the given url query string.
     */
    private static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            String[] split = param.split("=");
            String name = split[0];
            if (!name.isEmpty()) {
                String value = split.length > 1 ? split[1] : "";
                map.put(name, value);
            }
        }
        return map;
    }


    /**
     * Deletes every {@link CartItem} from {@link Cart} .
     *
     * @param session     must not be {@literal null}.
     * @param userAccount must not be {@literal null}.
     */
    @RequestMapping(value = "/deleteallitems", method = RequestMethod.DELETE)
    public String deleteAll(@ModelAttribute Cart cart, HttpSession session, @LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        cart.clear();
        if (cart.isEmpty()) {
            if (!((boolean) session.getAttribute("isReclaim")))
                session.setAttribute("isReclaim", true);
        }
        return "redirect:/cart";
    }


    /**
     * Returns a view of {@link Cart} after it´s emptiing.
     */
    @RequestMapping("/deleteallitems/")
    public String deleteAll(@LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        return "cart";
    }


    /**
     * Deletes {@link CartItem} from {@link Cart} .
     *
     * @param identifier  must not be {@literal null}.
     * @param session     must not be {@literal null}.
     * @param userAccount must not be {@literal null}.
     */
    @RequestMapping(value = "/deletecartitem/", method = RequestMethod.POST)
    public String deleteCartItem(@RequestParam String identifier, @ModelAttribute Cart cart, HttpSession session, @LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        cart.removeItem(identifier);
        if (cart.isEmpty()) {
            if (!((boolean) session.getAttribute("isReclaim")))
                session.setAttribute("isReclaim", true);
        }

        return "redirect:/cart";
    }

    /**
     * Returns a view of {@link Cart} after deleting of {@link CartItem}.
     */
    @RequestMapping("/deletecartitem/")
    public String deleteCartItem(@LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        return "cart";
    }


    /**
     * Updates {@link CartItem} of {@link Cart} .
     *
     * @param identifier  must not be {@literal null}.
     * @param quantity    must not be {@literal null}.
     * @param session     must not be {@literal null}.
     * @param userAccount must not be {@literal null}.
     */
    @RequestMapping(value = "/updatecartitem/", method = RequestMethod.POST)
    public String updateCartItem(@RequestParam String identifier, @RequestParam String quantity, @ModelAttribute Cart cart, HttpSession session, @LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";
        int oldquantity = Integer.parseInt(cart.getItem(identifier).get().getQuantity().getAmount().toString());
        int newquantity = Integer.parseInt(quantity);
        if (newquantity <= 0) {
            newquantity = 0;
        }
        int updatequantity = newquantity - oldquantity;

        String onLager = (inventory.findByProductIdentifier(cart.getItem(identifier).get().getProduct().getIdentifier())).get().getQuantity().getAmount().toString();
        System.out.println("Am Lager sind so viele Produkte: " + onLager);
        if (Integer.parseInt(onLager) <= newquantity) {
            updatequantity = Integer.parseInt(onLager) - oldquantity;
        }

        cart.addOrUpdateItem(cart.getItem(identifier).get().getProduct(), new Quantity(updatequantity, cart.getItem(identifier).get().getQuantity().getMetric(), cart.getItem(identifier).get().getQuantity().getRoundingStrategy()));
        return "redirect:/cart";
    }

    /**
     * Returns a view of {@link Cart} after updating of {@link CartItem}.
     */
    @RequestMapping("/updatecartitem/")
    public String updateCartItem(@LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        return "cart";
    }


    /**
     * Returns the overview of {@link Cart}.
     *
     * @param userAccount must not be {@literal null}.
     */
    @RequestMapping("/checkout")
    public String checkout(@LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        return "checkout";
    }


    /**
     * Generates {@link PaymentMethod} from given {@link PaymentType}.
     */
    public PaymentMethod strToPaymentMethod(String strPayment) {
        //because of a bug in PaymentMethod class the way of generating is scheduled with set data
        PaymentMethod paymentMethod;
        LocalDateTime dateWritten = LocalDateTime.now();
        LocalDateTime validFrom = LocalDateTime.parse("2013-12-18T14:30");
        LocalDateTime expiryDate = LocalDateTime.parse("2020-12-18T14:30");
        org.joda.money.Money dailyWithdrawalLimit = org.joda.money.Money.of(CurrencyUnit.EUR, 1000);
        org.joda.money.Money creditLimit = org.joda.money.Money.of(CurrencyUnit.EUR, 1000);
        String p = " ";
        System.out.println(strPayment);
        //compares given String with existing methods and generates an required paymentMethod
        if (strPayment.equals("CASH")) {
            paymentMethod = new Cash();
            return paymentMethod;
        } else if (strPayment.equals("CHEQUE")) {
            paymentMethod = new Cheque(p, p, p, p, dateWritten, p, p, p);
            return paymentMethod;
        } else if (strPayment.equals("CREDITCARD")) {
            paymentMethod = new CreditCard(p, p, p, p, p, validFrom, expiryDate, p, dailyWithdrawalLimit, creditLimit);
            System.out.println(strPayment + " " + paymentMethod.toString() + "   allright");
            return paymentMethod;
        }
        return new Cash();
    }


    /**
     * Returns an overview of current order.
     *
     * @param userAccount must not be {@literal null}.
     */
    @RequestMapping("/orderoverview")
    public String orderoverview(@LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        return "orderoverview";
    }


    /**
     * Checks out the current state of the {@link Cart} and creates new GSOrder from {@link CartItem} Using a method parameter of type {@code Optional<UserAccount>}
     * annotated with {@link LoggedIn} you can access the {@link UserAccount} of the currently logged in user.
     *
     * @param session     must not be {@literal null}.
     * @param userAccount must not be {@literal null}.
     * @param model       must not be {@literal null}.
     * @return
     */
    @RequestMapping(value = "/buy", method = RequestMethod.POST)
    public String buy(@ModelAttribute Cart cart, HttpSession session, @RequestParam String payment, @LoggedIn final Optional<UserAccount> userAccount, Model model) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        return userAccount.map(account -> {

            PaymentMethod paymentM = strToPaymentMethod(payment);
            GSOrder order = new GSOrder(userAccount.get(), paymentM);

            System.out.println(paymentM.toString());

            for (CartItem cartItem : cart) {
                order.add(new GSOrderLine(cartItem.getProduct(), cartItem.getQuantity()));
            }

            order.pay();
            orderRepo.save(order);

            cart.clear();
            model.addAttribute("order", order);
            if (!((boolean) session.getAttribute("isReclaim"))) {
                session.setAttribute("isReclaim", true);
            }
            session.setAttribute("overview", true);

            model.addAttribute("catalog", catalog);

            return "orderoverview";
        }).orElse("redirect:/cart");

    }

    /**
     * Returns a view of after buying.
     */
    @RequestMapping("/buy")
    public String buy(@LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        return "redirect:/productsearch";
    }
}