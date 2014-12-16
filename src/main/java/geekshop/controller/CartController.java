package geekshop.controller;

/*
 * Created by Basti on 20.11.2014.
 */

import geekshop.model.*;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.Product;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.order.Cart;
import org.salespointframework.order.OrderManager;
import org.salespointframework.payment.Cash;
import org.salespointframework.payment.Cheque;
import org.salespointframework.payment.CreditCard;
import org.salespointframework.payment.PaymentMethod;
import org.salespointframework.quantity.Units;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.time.Interval;
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
import java.util.Optional;
import java.util.TimeZone;

/**
 * A Spring MVC controller to manage the {@link org.salespointframework.order.Cart}.
 *
 * @author Sebastian D&ouml;ring
 */

@Controller
@PreAuthorize("isAuthenticated()")
@SessionAttributes("cart")
class CartController {
    private PaymentMethod paymentMethod;
    private final OrderManager<GSOrder> orderManager;
    private final Inventory<GSInventoryItem> inventory;
    private final BusinessTime businessTime;
    private final Catalog<GSProduct> catalog;
    private final PasswordRules passwordRules;
    private final UserRepository userRepo;
    private final GSOrderRepository orderRepo;


    /**
     * Creates a new {@link CartController} with the given {@link OrderManager}.
     *
     * @param orderManager must not be {@literal null}.
     */
    @Autowired
    public CartController(OrderManager<GSOrder> orderManager, Inventory<GSInventoryItem> inventory, BusinessTime businessTime, Catalog<GSProduct> catalog, PasswordRulesRepository passRulesRepo, UserRepository userRepo, GSOrderRepository orderRepo) {

        Assert.notNull(orderManager, "OrderManager must not be null!");
        this.orderManager = orderManager;
        this.inventory = inventory;
        this.businessTime = businessTime;
        this.catalog = catalog;
        this.passwordRules = passRulesRepo.findOne("passwordRules").get();
        this.userRepo = userRepo;
        this.orderRepo= orderRepo;
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
    public String cart(Model model, @LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return AccountController.adjustPW(model, user, passwordRules);

        return "cart";
    }

    @RequestMapping("/reclaim")
    public String reclaim(Model model, @LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return AccountController.adjustPW(model, user, passwordRules);

        return "reclaim";
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
    @RequestMapping(value = "/cart", method = RequestMethod.POST)

    public String addProductToCart(@RequestParam("pid") Product product, @RequestParam("number") long number, @ModelAttribute Cart cart,
                                   Model model, @LoggedIn Optional<UserAccount> userAccount) {


        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return AccountController.adjustPW(model, user, passwordRules);

        if (number <= 0) {
            number = 1;
        }
        if (number > inventory.findByProduct(product).get().getQuantity().getAmount().intValueExact()) {
            number = inventory.findByProduct(product).get().getQuantity().getAmount().intValueExact();
        }
        ;

        cart.addOrUpdateItem(product, Units.of(number));
        return "redirect:/productsearch";

    }


    @RequestMapping(value = "/deleteallitems", method = RequestMethod.DELETE)
    public String deleteAll(Model model, @ModelAttribute Cart cart, @LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return AccountController.adjustPW(model, user, passwordRules);

        cart.clear();
        return "redirect:/cart";
    }

    @RequestMapping(value = "/deletecartitem/", method = RequestMethod.POST)
    public String deleteCartItem(@RequestParam String identifier, @ModelAttribute Cart cart, Model model, @LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return AccountController.adjustPW(model, user, passwordRules);

        cart.removeItem(identifier);
        return "redirect:/cart";
    }


    @RequestMapping(value = "/cart", method = RequestMethod.GET)
    public String basket(Model model, @LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return AccountController.adjustPW(model, user, passwordRules);

        return "cart";
    }

    @RequestMapping("/checkout")
    public String checkout(Model model, @LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return AccountController.adjustPW(model, user, passwordRules);

        return "checkout";
    }


    @RequestMapping(value = "/chosepaymentmethod", method = RequestMethod.POST)
    public PaymentMethod strToPaymentMethod(@RequestParam ("paymentMethod") String strPayment,
                                            @RequestParam ("accountname") String accountName,
                                            @RequestParam ("accountnumber")String accountNumber,
                                            @RequestParam ("chequenumber") String chequeNumber,
                                            @RequestParam ("payee") String payee,
                                            @RequestParam ("bankname") String bankName,
                                            @RequestParam ("bankaddress") String bankAddress,
                                            @RequestParam ("bankid")String bankIdentificationNumber,
                                            @RequestParam ("cardname")String cardName,
                                            @RequestParam ("cardassociationname") String cardAssociationName,
                                            @RequestParam ("cardnumber")String cardNumber,
                                            @RequestParam ("nameoncard")String nameOnCard,
                                            @RequestParam ("billingadress")String billingAddress,
                                            @RequestParam ("cardverificationcode")String cardVerificationCode) {
        PaymentMethod paymentMethod;
        LocalDateTime dateWritten= LocalDateTime.now();
        LocalDateTime validFrom= LocalDateTime.parse("2013-12-18T14:30");  //später ändern
        LocalDateTime expiryDate=LocalDateTime.parse("2020-12-18T14:30");  //später ändern
        org.joda.money.Money dailyWithdrawalLimit=  org.joda.money.Money.parse("1000");
        org.joda.money.Money creditLimit=  org.joda.money.Money.parse("1000");

        if (strPayment.equals("Barzahlung")) {paymentMethod = new Cash(); return paymentMethod;}
        else if (strPayment.equals("Lastshriftverfahren")) {paymentMethod = new Cheque(accountName, accountNumber, chequeNumber,payee, dateWritten, bankName,
                bankAddress,bankIdentificationNumber); return paymentMethod;}
        else if (strPayment.equals("Kreditkarte")) {paymentMethod= new CreditCard(cardName, cardAssociationName, cardNumber,
                nameOnCard, billingAddress, validFrom, expiryDate, cardVerificationCode, dailyWithdrawalLimit, creditLimit); return paymentMethod;}
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
    public String orderoverview(Model model, @LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return AccountController.adjustPW(model, user, passwordRules);

        return "orderoverview";
    }



    @RequestMapping(value = "/buy", method = RequestMethod.POST)
  public String buy(Model model, @ModelAttribute Cart cart, @LoggedIn final Optional<UserAccount> userAccount, String strPayment) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return AccountController.adjustPW(model, user, passwordRules);


               return userAccount.map(account -> {

//                  // (｡◕‿◕｡)
//                    // Mit commit wird der Warenkorb in die Order überführt, diese wird dann bezahlt und abgeschlossen.
//                    // Orders können nur abgeschlossen werden, wenn diese vorher bezahlt wurden.



           long orderNumber= Calendar.getInstance(TimeZone.getDefault()).getTime().getTime();
           String strNumber= Long.toString(orderNumber);      //generating new orderIdentifier


          // GSOrder order = new GSOrder( orderNumber, userAccount.get(), strToPaymentMethod(strPayment, ));

      //     cart.addItemsTo(order);

          // LocalDateTime time= LocalDateTime.now();           //setting time of buying
         //  order.setDateCreated(time);

       // LocalDateTime timeup= time.plusDays(14);


        //  Interval interval=new Interval(time, time);

  //                orderManager.payOrder(order);
                 //  orderManager.completeOrder(order);
   //                 orderManager.save(order);
    //               orderRepo.save(order);


                   cart.clear();

                 return "orderoverview";
               }).orElse("redirect:/cart");

        //return "orderoverview";
    }

//        public void acceptReclaim(){
//            //orderline.state='reclaimed';
//        }

}