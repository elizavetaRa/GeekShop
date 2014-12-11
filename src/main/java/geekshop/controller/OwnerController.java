package geekshop.controller;

/*
 * Created by Basti on 20.11.2014.
 */

import geekshop.model.*;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.order.OrderLine;
import org.salespointframework.order.OrderManager;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountIdentifier;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * A Spring MVC controller to manage the shop owner's functions.
 *
 * @author Felix D&ouml;ring
 * @author Sebastian D&ouml;ring
 * @author Dominik Lauck
 */

@Controller
@PreAuthorize("hasRole('ROLE_OWNER')")
class OwnerController {
    private final OrderManager<GSOrder> orderManager;
    private final GSOrderRepository orderRepo;
    private final Catalog<GSProduct> catalog;
    private final UserRepository userRepo;
    private final JokeRepository jokeRepo;
    private final UserAccountManager userAccountManager;
    private final MessageRepository messageRepo;
    private final PasswordRules passwordRules;

    /*@RequestMapping("/orders")
    public String orders(Catalog<GSProduct> catalog, OrderManager<GSOrder> orderManager) {
        Iterable<UserAccount> userAccountList = userAccountManager.findAll();                                               //1. Iterable mit allen userAccounts erstellen
        List<GSOrder> ol = new ArrayList<GSOrder>();                                                                        //2. neue Liste über GSOrder
        for (Iterator<UserAccount> uaList = userAccountList.iterator(); uaList.hasNext(); ) {                                 //3. für alle Elemente der userAccountListe
            Iterable<GSOrder> myOrderList = orderManager.find(uaList.next());                                               //4. wird eine weitere Liste über GSOrderLines erstellt (jede enthält nur order eines userAccounts)
            for (Iterator<GSOrder> orderList = myOrderList.iterator(); orderList.hasNext(); ) {                               //5. jedes Element dieser neuerstellten Listen
                ol.add(orderList.next());                                                                                   //6. wird zu der ursprünglichen orderListe hizugefügt (in 2.)
            }
        }

        Iterable<GSProduct> productList = catalog.findAll();                                                                //7. Iterable mit allen Produkten erstellen

        Map<GSProduct, List<GSProductOrder>> map = new HashMap<GSProduct, List<GSProductOrder>>();                          //8. die Map wird initialisiert
        List<GSProductOrder> productOrderList = new ArrayList<GSProductOrder>();                                            //9. die Liste über die GSProductOrder wird erstellt
        for (Iterator<GSProduct> prodList = productList.iterator(); prodList.hasNext(); ) {                                  //10. für jedes Product aus der Liste (in 8.)
            for (Iterator<GSOrder> oList = ol.iterator(); oList.hasNext(); ) {                                                //11. und für jede order aus der Liste (in 2.)
                if (oList.next().getOrderLines().iterator().next().getProductName().equals(prodList.next().getName())) {     //12. wird geprüft, ob die orderLine zu dem aktuellen Produkt gehört
                    GSOrderLine gsol = (GSOrderLine) oList.next().getOrderLines();                                           //13. die orderLine wird zwischengespeichert
                    User user = userRepo.findByUserAccount(oList.next().getUserAccount());                                  //14. der Verkäufer der aktuellen order wird ermittelt
                    GSProductOrder productOrder = new GSProductOrder(gsol, oList.next().getDateCreated(), user);            //15. mit der orderLine (in 13.), dem datum der order und dem Verkäufer (in 14.) wird die GSProductOrder erstellt
                    productOrderList.add(productOrder);                                                                     //16. diese GSProductOrder wird einer Liste hinzugefügt
                    map.put(prodList.next(), productOrderList);                                                             //17. in die Map werden die Produkte mit einer Liste der zugehörigen OrderLines gespeichert
                }
            }
        }
        return "orders";
    }*/

    @Autowired
    public OwnerController(GSOrderRepository orderRepo, OrderManager<GSOrder> orderManager, Catalog<GSProduct> catalog, UserRepository userRepo, JokeRepository jokeRepo, UserAccountManager userAccountManager, MessageRepository messageRepo, PasswordRulesRepository passRulesRepo) {
        this.orderManager = orderManager;
        this.orderRepo = orderRepo;
        this.catalog = catalog;
        this.userRepo = userRepo;
        this.jokeRepo = jokeRepo;
        this.userAccountManager = userAccountManager;
        this.messageRepo = messageRepo;
        this.passwordRules = passRulesRepo.findOne("passwordRules").get();
    }

    @RequestMapping("/orders")
    public String orders(Model model) {

        Map<GSProduct, GSProductOrders> map = new HashMap<GSProduct, GSProductOrders>();

        // for each GSProduct create map entry
        for (GSProduct product : catalog.findAll()) {
            map.put(product, new GSProductOrders());
        }

//        GSOrder recOrder = orderManager.find(userAccountManager.findByUsername("owner").get()).iterator().next();
//        GSOrderLine recOl = (GSOrderLine) recOrder.getOrderLines().iterator().next();
//        recOl.increaseReclaimedAmount(BigDecimal.valueOf(5));
//        orderManager.save(recOrder);

        for (GSOrder order : orderRepo.findAll()) {
            if (order.getOrderType() != OrderType.RECLAIM) {    // reclaim orders ought not to be shown
                createProductOrder(map, order);
            }
        }

//        for (GSOrder order : orderManager.find(OrderStatus.COMPLETED)) {
//            if (order.getOrderType() != OrderType.RECLAIM) {    // reclaim orders ought not to be shown
//                createProductOrder(map, order);
//            }
//        }

        model.addAttribute("orders", map);

        return "orders";
    }

    private void createProductOrder(Map<GSProduct, GSProductOrders> map, GSOrder order) {
        LocalDateTime date = order.getDateCreated();    // date
        UserAccount ua = order.getUserAccount();
        User seller = userRepo.findByUserAccount(ua);   // seller
        for (OrderLine ol : order.getOrderLines()) {    // add each orderline to the respetive map entry
            GSProductOrder productOrder = new GSProductOrder((GSOrderLine) ol, date, seller);
            GSProduct product = catalog.findOne(ol.getProductIdentifier()).get();
            GSProductOrders prodOrders = map.get(product);
            if (prodOrders != null)
                prodOrders.addProductOrder(productOrder);
        }
    }

    @RequestMapping("/jokes")
    public String jokes(Model model) {
        model.addAttribute("jokes", jokeRepo.findAll());
        return "jokes";
    }

    @RequestMapping(value = "/newjoke", method = RequestMethod.POST)
    public String newJoke(@RequestParam("newJoke") String text) {
        jokeRepo.save(new Joke(text));
        return "redirect:/jokes";
    }

    @RequestMapping(value = "/jokes/{id}", method = RequestMethod.POST)
    public String showJoke(Model model, @PathVariable("id") Long id) {
        Joke joke = jokeRepo.findJokeById(id);
        model.addAttribute("joke", joke);
        return "editjoke";
    }

    @RequestMapping(value = "/editjoke/{id}", method = RequestMethod.POST)
    public String editJoke(@PathVariable("id") Long id, @RequestParam("jokeText") String jokeText) {
        Joke joke = jokeRepo.findJokeById(id);
        joke.setText(jokeText);
        jokeRepo.save(joke);
        return "redirect:/jokes";
    }

    @RequestMapping("/staff")
    public String staff(Model model) {

        List<User> employees = getEmployees();
        model.addAttribute("staff", employees);

        return "staff";
    }

    @RequestMapping("/messages")
    public String messages(Model model) {
        model.addAttribute("ownermessage", messageRepo.findAll());
        return "messages";
    }

    @RequestMapping(value = "/messages/{id}", method = RequestMethod.DELETE)
    public String deleteMessage(@PathVariable("id") Long id) {
        messageRepo.delete(id);
        return "redirect:/messages";
    }

    @RequestMapping("/addemployee")
    public String hire() {
        return "addemployee";
    }

    @RequestMapping(value = "/addemployee", method = RequestMethod.POST)
    public String hire(@RequestParam("username") String username,
                       @RequestParam("firstname") String firstname,
                       @RequestParam("mail") String mail,
                       @RequestParam("lastname") String lastname,
                       @RequestParam("gender") String strGender,
                       @RequestParam("birthday") String strBirthday,
                       @RequestParam("maritalStatus") String strMaritalStatus,
                       @RequestParam("phone") String phone,
                       @RequestParam("street") String street,
                       @RequestParam("houseNr") String houseNr,
                       @RequestParam("postcode") String postcode,
                       @RequestParam("place") String place) {

        String password = passwordRules.generateRandomPassword();
        String messageText = "Startpasswort des Nutzers" + username + ": " + password;
        messageRepo.save(new Message(MessageKind.NOTIFICATION, messageText));
        UserAccount newUserAccount = userAccountManager.create(username, password, new Role("ROLE_EMPLOYREE"));
        newUserAccount.setFirstname(firstname);
        newUserAccount.setLastname(lastname);
        newUserAccount.setEmail(mail);
        userAccountManager.save(newUserAccount);
        Gender gender = strToGen(strGender);
        Date birthday = strToDate(strBirthday);
        if (birthday == null) return "/addemployee";
        MaritalStatus maritalStatus = strToMaritialStatus(strMaritalStatus);

        User newUser = new User(newUserAccount, gender, birthday, maritalStatus, phone, street, houseNr, postcode, place);
        userRepo.save(newUser);

        return "redirect:/staff";
    }

//    @RequestMapping("/staff/{username}")
//    public String showEmployee(Model model, @PathVariable("username") UserAccountIdentifier username) {
//        UserAccount userAccount = userAccountManager.get(username).get();
//        User user = userRepo.findByUserAccount(userAccount);
//        model.addAttribute("user", user);
//
//        return "profile";
//    }

    @RequestMapping("/staff/{uai}")
    public String showEmployee(Model model, @PathVariable("uai") UserAccountIdentifier uai) {
        UserAccount userAccount = userAccountManager.get(uai).get();
        User user = userRepo.findByUserAccount(userAccount);
        model.addAttribute("user", user);
        model.addAttribute("isOwnProfile", false);

        return "profile";
    }

    @RequestMapping(value = "/staff/{username}", method = RequestMethod.DELETE)
    public String fire(@PathVariable("username") UserAccountIdentifier username) {
        UserAccount userAccount = userAccountManager.get(username).get();
        Role role = new Role("ROLE_OWNER");
        if (userAccount.hasRole(role)) {
            return "redirect:/staff";
        } else {
            Long id = userRepo.findByUserAccount(userAccount).getId();
            userRepo.delete(id);
        }
        return "redirect:/staff";
    }

//    @RequestMapping(value = "/staff/firemany", method = RequestMethod.DELETE)
//    public String fireMany(){
//
//        List<User> employees = getEmployees();
//        List<Long> ids = new LinkedList<Long>();
//        for (User user : employees) {
//            if(employees.iterator().
//        }
//
//        return "redirect:/staff";
//    }

    public static Date strToDate(String strDate) {
        strDate = strDate.replace(".", " ");
        strDate = strDate.replace("-", " ");
        strDate = strDate.replace("/", " ");
        Date date = null;
        try {
            date = new SimpleDateFormat("dd MM yyyy").parse(strDate);
        } catch (ParseException e) {

        }

        return date;

    }

    public MaritalStatus strToMaritialStatus(String strMaritalStatus) {
        MaritalStatus maritalStatus;
        if (strMaritalStatus.equals("UNMARRIED")) maritalStatus = MaritalStatus.UNMARRIED;
        if (strMaritalStatus.equals("MARRIED")) maritalStatus = MaritalStatus.MARRIED;
        if (strMaritalStatus.equals("SEPARATED")) maritalStatus = MaritalStatus.SEPARATED;
        if (strMaritalStatus.equals("DIVORCED")) maritalStatus = MaritalStatus.DIVORCED;
        if (strMaritalStatus.equals("WIDOWED")) maritalStatus = MaritalStatus.WIDOWED;
        if (strMaritalStatus.equals("PARTNERED")) maritalStatus = MaritalStatus.PARTNERED;
        if (strMaritalStatus.equals("NO_MORE_PARTNERED")) maritalStatus = MaritalStatus.NO_MORE_PARTNERED;
        if (strMaritalStatus.equals("PARTNER_LEFT_BEHIND")) maritalStatus = MaritalStatus.PARTNER_LEFT_BEHIND;
        else maritalStatus = MaritalStatus.UNKNOWN;

        return maritalStatus;
    }


    public Gender strToGen(String strGender) {
        Gender gender;
        if (strGender.equals("m")) gender = Gender.MALE;
        else if (strGender.equals("f")) gender = Gender.FEMALE;
        else gender = Gender.SOMETHING_ELSE;
        return gender;
    }

    public List<User> getEmployees() {
        Iterable<User> allUsers = userRepo.findAll();
        List<User> employees = new LinkedList<User>();
        for (User user : allUsers) {
            if (!user.getUserAccount().hasRole(new Role("ROLE_OWNER"))) {
                employees.add(user);
            }
        }
        return employees;
    }
}
