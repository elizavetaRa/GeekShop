package geekshop.controller;

import geekshop.model.*;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.ProductIdentifier;
import org.salespointframework.order.OrderIdentifier;
import org.salespointframework.order.OrderLine;
import org.salespointframework.order.OrderManager;
import org.salespointframework.useraccount.UserAccount;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    private final SubCategoryRepository subCategoryRepo;
    private final SuperCategoryRepository superCategoryRepo;


    @Autowired
    public OwnerController(GSOrderRepository orderRepo, OrderManager<GSOrder> orderManager, Catalog<GSProduct> catalog,
                           UserRepository userRepo, JokeRepository jokeRepo, UserAccountManager userAccountManager,
                           MessageRepository messageRepo, SubCategoryRepository subCategoryRepo,
                           SuperCategoryRepository superCategoryRepo) {
        this.orderManager = orderManager;
        this.orderRepo = orderRepo;
        this.catalog = catalog;
        this.userRepo = userRepo;
        this.jokeRepo = jokeRepo;
        this.userAccountManager = userAccountManager;
        this.messageRepo = messageRepo;
        this.subCategoryRepo = subCategoryRepo;
        this.superCategoryRepo = superCategoryRepo;
    }


    @RequestMapping("/orders")
    public String orders(Model model) {

        Map<GSProduct, GSProductOrders> map = new HashMap<GSProduct, GSProductOrders>();

        // for each GSProduct create map entry
        for (GSProduct product : catalog.findAll()) {
            map.put(product, new GSProductOrders());
        }

        for (GSOrder order : orderRepo.findAll()) {
            if (!order.isOpen() && !order.isCanceled()) {    // open and canceled orders ought not to be shown
                createProductOrder(map, order);
            }
        }

        model.addAttribute("orders", map);

        return "orders";
    }

    private void createProductOrder(Map<GSProduct, GSProductOrders> map, GSOrder order) {
        LocalDateTime ldt = order.getDateCreated();    // date
        ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
        Date date = Date.from(zdt.toInstant());

        UserAccount ua = order.getUserAccount();
        User seller = userRepo.findByUserAccount(ua);   // seller
        for (OrderLine ol : order.getOrderLines()) {    // add each orderline to the respective map entry
            GSProductOrder productOrder = new GSProductOrder((GSOrderLine) ol, date, seller);
            GSProduct product = catalog.findOne(ol.getProductIdentifier()).get();
            GSProductOrders prodOrders = map.get(product);
            if (prodOrders != null)
                prodOrders.addProductOrder(productOrder);
        }
    }

    @RequestMapping(value = "/showreclaim/reclaim={rid}", method = RequestMethod.POST)
    public String showReclaim(Model model, @PathVariable("rid") OrderIdentifier reclaimId, @RequestParam("msgId") Long msgId) {

        Set<ReclaimTupel> products = new HashSet<>();
        GSOrder order = orderRepo.findOne(reclaimId).get();

        for (OrderLine line : order.getOrderLines()) {
            products.add(new ReclaimTupel(catalog.findOne(line.getProductIdentifier()).get(), line));
        }
        Message message = messageRepo.findOne(msgId).get();
        model.addAttribute("rid", reclaimId);
        model.addAttribute("message", message);
        model.addAttribute("products", products);
        model.addAttribute("order", order);

        return "showreclaim";
    }

    @RequestMapping(value = "/showreclaim/reclaim={rid}", method = RequestMethod.DELETE)
    public String acceptReclaim(@PathVariable("rid") OrderIdentifier reclaimId, @RequestParam("msgId") Long msgId, @RequestParam("accept") Boolean accept) {
        messageRepo.delete(msgId);
        if (accept == true) {
            //ReaddItemstoStock
        }

        return "redirect:/messages";
    }


    @RequestMapping("/jokes")
    public String jokes(Model model) {
        model.addAttribute("jokes", jokeRepo.findAll());
        return "jokes";
    }

    @RequestMapping("/newjoke")
    public String newJoke() {
        return "editjoke";
    }

    @RequestMapping(value = "/newjoke", method = RequestMethod.POST)
    public String newJoke(@RequestParam("jokeText") String text) {
        jokeRepo.save(new Joke(text));
        return "redirect:/jokes";
    }

    @RequestMapping("/jokes/{id}")
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

    @RequestMapping(value = "/jokes/{id}", method = RequestMethod.DELETE)
    public String deleteJoke(@PathVariable("id") Long id) {
        Joke joke = jokeRepo.findJokeById(id);
        Iterable<User> allUsers = userRepo.findAll();
        for (User user : allUsers) {
            List<Joke> recentJokes = user.getRecentJokes();
            recentJokes.removeAll(Collections.singletonList(joke));
            userRepo.save(user);
        }

        jokeRepo.delete(joke);
        return "redirect:/jokes";
    }

    @RequestMapping(value = "/deljokes", method = RequestMethod.DELETE)
    public String deleteAllJokes() {
        Iterable<User> allUsers = userRepo.findAll();
        for (User user : allUsers) {
            List<Joke> recentJokes = user.getRecentJokes();
            recentJokes.clear();
            userRepo.save(user);
        }
        jokeRepo.deleteAll();
        return "redirect:/jokes";
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

    @RequestMapping("/range")
    public String range(Model model) {
        model.addAttribute("subcategories", subCategoryRepo.findAll());
        model.addAttribute("supercategories", superCategoryRepo.findAll());
        return "range";
    }

    @RequestMapping(value = "/range/delsuper", method = RequestMethod.DELETE)
    public String delSuper(@RequestParam("superName") String superName) {

        SuperCategory superCategory = superCategoryRepo.findByName(superName);

        List<SubCategory> sub = superCategory.getSubCategories();

        int i = 0;

        while (!sub.isEmpty()) {

            delSub(sub.remove(i));

        }

        superCategoryRepo.delete(superCategory.getId());
        return "redirect:/range";
    }

    @RequestMapping(value = "/range/delsub", method = RequestMethod.DELETE)
    public String delSubRequest(@RequestParam("subName") String subName) {

        SubCategory subCategory = subCategoryRepo.findByName(subName);

        SuperCategory superCategory = subCategory.getSuperCategory();

        superCategory.getSubCategories().remove(subCategory);


        delSub(subCategory);


        return "redirect:/range";
    }

    @RequestMapping(value = "/range/delproduct", method = RequestMethod.DELETE)
    public String delProductRequest(@RequestParam("productIdent") ProductIdentifier productIdentifier) {
        GSProduct product = catalog.findOne(productIdentifier).get();
        product.getSubCategory().getProducts().remove(product.getSubCategory().getProducts().indexOf(product));
        delProduct(productIdentifier);

        return "redirect:/range";
    }

    public void delSub(SubCategory subCategory) {
        for (GSProduct product : subCategory.getProducts()) {
            ProductIdentifier productIdentifier = product.getIdentifier();
            delProduct(productIdentifier);
        }
        Long id = subCategory.getId();
        subCategoryRepo.delete(id);

    }

    public void delProduct(ProductIdentifier productIdentifier) {
        GSProduct product = catalog.findOne(productIdentifier).get();
        product.setInRange(false);
        product.setSubCategory(null);
    }


}
