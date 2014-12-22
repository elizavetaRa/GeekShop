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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
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
class CartController {
    private final Inventory<GSInventoryItem> inventory;
    private final BusinessTime businessTime;
    private final Catalog<GSProduct> catalog;
    private final UserRepository userRepo;
    private final GSOrderRepository orderRepo;

    @Autowired
    public CartController(Inventory<GSInventoryItem> inventory, BusinessTime businessTime, Catalog<GSProduct> catalog, UserRepository userRepo, GSOrderRepository orderRepo) {

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


    @RequestMapping("/cart")
    public String cart(@LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        return "cart";
    }


    @RequestMapping(value = "/cart", method = RequestMethod.POST)

    public String addProductToCart(@RequestParam("pid") Product product, @RequestParam("number") long number, @ModelAttribute Cart cart, HttpSession session, @LoggedIn Optional<UserAccount> userAccount, Model model) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        if ((boolean) session.getAttribute("isReclaim"))
            session.setAttribute("isReclaim", false);

        if (number <= 0) {
           /* number = 1;*/
            return "redirect:/productsearch";
        }
        if (number > inventory.findByProduct(product).get().getQuantity().getAmount().intValueExact()) {
            number = inventory.findByProduct(product).get().getQuantity().getAmount().intValueExact();
        }

        System.out.println("Anzahl Produkte vor dem hinzufügen zu Cart  " + inventory.findByProduct(product).get().getQuantity().getAmount().intValueExact());
        CartItem item = cart.addOrUpdateItem(product, Units.of(number));
        System.out.println("zu Cart hinzugefügt:  " + number);

        if (item.getQuantity().getAmount().intValueExact() >= inventory.findByProduct(product).get().getQuantity().getAmount().intValueExact()
                ) {
            cart.removeItem(item.getIdentifier());
            cart.addOrUpdateItem(product, inventory.findByProduct(product).get().getQuantity());
        }

        return "redirect:/productsearch";

    }


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


    @RequestMapping(value = "/cart", method = RequestMethod.GET)
    public String basket(@LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        return "cart";
    }

    @RequestMapping("/checkout")
    public String checkout(@LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        return "checkout";
    }


    @RequestMapping(value = "/chosepaymentmethod", method = RequestMethod.POST)
    public PaymentMethod strToPaymentMethod(String strPayment)
//                                            @RequestParam("accountname") String accountName,
//                                            @RequestParam("accountnumber") String accountNumber,
//                                            @RequestParam("chequenumber") String chequeNumber,
//                                            @RequestParam("payee") String payee,
//                                            @RequestParam("bankname") String bankName,
//                                            @RequestParam("bankaddress") String bankAddress,
//                                            @RequestParam("bankid") String bankIdentificationNumber,
//                                            @RequestParam("cardname") String cardName,
//                                            @RequestParam("cardassociationname") String cardAssociationName,
//                                            @RequestParam("cardnumber") String cardNumber,
//                                            @RequestParam("nameoncard") String nameOnCard,
//                                            @RequestParam("billingadress") String billingAddress,
//                                            @RequestParam("cardverificationcode") String cardVerificationCode) {
    {
        PaymentMethod paymentMethod;

        LocalDateTime dateWritten = LocalDateTime.now();
        LocalDateTime validFrom = LocalDateTime.parse("2013-12-18T14:30");
        LocalDateTime expiryDate = LocalDateTime.parse("2020-12-18T14:30");
        org.joda.money.Money dailyWithdrawalLimit = org.joda.money.Money.of(CurrencyUnit.EUR, 1000);
        org.joda.money.Money creditLimit = org.joda.money.Money.of(CurrencyUnit.EUR, 1000);
        String p = " ";
        System.out.println(strPayment);

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
     * Checks out the current state of the {@link Cart}. Using a method parameter of type {@code Optional<UserAccount>}
     * annotated with {@link LoggedIn} you can access the {@link UserAccount} of the currently logged in user.
     *
     * @param userAccount must not be {@literal null}.
     * @return
     */

    @RequestMapping("/orderoverview")
    public String orderoverview(@LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        return "orderoverview";
    }


    @RequestMapping(value = "/buy", method = RequestMethod.POST)
    public String buy(@ModelAttribute Cart cart, HttpSession session, @RequestParam /*Map<String, String> map*/ String payment, @LoggedIn final Optional<UserAccount> userAccount, Model model) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        return userAccount.map(account -> {

            PaymentMethod paymentM = strToPaymentMethod(payment);
            GSOrder order = new GSOrder(userAccount.get(), paymentM);

            System.out.println(paymentM.toString());
            // eigentlich cart.addItemsTo(order); Wir brauchen aber GSOrderLines!

            for (Iterator<CartItem> iterator = cart.iterator(); iterator.hasNext(); ) {
                CartItem cartItem = iterator.next();
                order.add(new GSOrderLine(cartItem.getProduct(), cartItem.getQuantity()));
            }

            order.pay();
            //  order.complete();
            orderRepo.save(order);

            cart.clear();
            model.addAttribute("order", order);
            if (!((boolean) session.getAttribute("isReclaim")))
                session.setAttribute("isReclaim", true);
            return "orderoverview";
        }).orElse("redirect:/cart");


    }


}