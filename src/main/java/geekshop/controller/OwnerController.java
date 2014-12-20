package geekshop.controller;

import geekshop.model.*;
import org.joda.money.Money;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.ProductIdentifier;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.order.OrderIdentifier;
import org.salespointframework.order.OrderLine;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.quantity.Units;
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
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static org.joda.money.CurrencyUnit.EUR;

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
    private final GSOrderRepository orderRepo;
    private final Catalog<GSProduct> catalog;
    private final UserRepository userRepo;
    private final JokeRepository jokeRepo;
    private final UserAccountManager userAccountManager;
    private final MessageRepository messageRepo;
    private final SubCategoryRepository subCategoryRepo;
    private final SuperCategoryRepository superCategoryRepo;
    private final Inventory<GSInventoryItem> inventory;


    @Autowired
    public OwnerController(GSOrderRepository orderRepo, Catalog<GSProduct> catalog,
                           UserRepository userRepo, JokeRepository jokeRepo, UserAccountManager userAccountManager,
                           MessageRepository messageRepo, SubCategoryRepository subCategoryRepo,
                           SuperCategoryRepository superCategoryRepo, Inventory<GSInventoryItem> inventory) {
        this.orderRepo = orderRepo;
        this.catalog = catalog;
        this.userRepo = userRepo;
        this.jokeRepo = jokeRepo;
        this.userAccountManager = userAccountManager;
        this.messageRepo = messageRepo;
        this.subCategoryRepo = subCategoryRepo;
        this.superCategoryRepo = superCategoryRepo;
        this.inventory = inventory;
    }


    @RequestMapping("/orders")
    public String orders(Model model) {

        Map<GSProduct, GSProductOrders> map = putMap();

        model.addAttribute("orders", map);

        return "orders";
    }

    private Map<GSProduct, GSProductOrders> putMap() {

        Map<GSProduct, GSProductOrders> map = new HashMap<GSProduct, GSProductOrders>();

        // for each GSProduct create map entry
        for (GSProduct product : catalog.findAll()) {
            map.put(product, new GSProductOrders());
        }

        for (GSOrder order : orderRepo.findAll()) {
            if (!order.isOpen() && !order.isCanceled()) { // open and canceled orders ought not to be shown
                createProductOrder(map, order);
            }
        }

        return map;
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

    @RequestMapping(value = "/exportXML", method = RequestMethod.POST)
    public String exportXML() {

        Map<GSProduct, GSProductOrders> map = putMap();

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Sales");
            doc.appendChild(rootElement);

            for (Map.Entry<GSProduct, GSProductOrders> entry : map.entrySet()) {
                // root elements
                Element Product = doc.createElement(entry.getKey().getName());
                rootElement.appendChild(Product);


                for (int i = 0; i < entry.getValue().getProductOrders().size(); i++) {
                    GSProductOrder element = entry.getValue().getProductOrders().get(i);

                    String olPrice;
                    String olQuantity;

                    if (element.getOrderLine().getType() == OrderType.RECLAIM) {
                        Money price = element.getOrderLine().getPrice().negated();
                        olPrice = price.toString();
                        olQuantity = "-" + element.getOrderLine().getQuantity().toString();
                    } else {
                        olPrice = element.getOrderLine().getPrice().toString();
                        olQuantity = element.getOrderLine().getQuantity().getAmount().toString();
                    }

                    // ProductOrder elements
                    Element Productorder = doc.createElement("Productorder");
                    Product.appendChild(Productorder);

                    // set attribute to Productorder element
                    Attr attr = doc.createAttribute("ID");
                    attr.setValue(String.valueOf(i));
                    Productorder.setAttributeNode(attr);

                    // Date elements
                    Element date = doc.createElement("Date");
                    date.appendChild(doc.createTextNode(element.getDate().toString()));
                    Productorder.appendChild(date);

                    // Seller elements
                    Element seller = doc.createElement("Seller");
                    seller.appendChild(doc.createTextNode(element.getSeller().toString()));
                    Productorder.appendChild(seller);

                    // Quantity elements
                    Element quantity = doc.createElement("Quantity");
                    quantity.appendChild(doc.createTextNode(olQuantity));
                    Productorder.appendChild(quantity);

                    // Price elements
                    Element price = doc.createElement("Price");
                    price.appendChild(doc.createTextNode(olPrice));
                    Productorder.appendChild(price);
                }


                // Total elements
                Element total = doc.createElement("Total");
                Product.appendChild(total);


                if (!entry.getValue().getProductOrders().isEmpty()) {
                    Attr attr = doc.createAttribute("Quantity");
                    attr.setValue(entry.getValue().getTotalQuantity().getAmount().toString());
                    total.setAttributeNode(attr);
                } else {
                    Attr attr = doc.createAttribute("Quantity");
                    attr.setValue(String.valueOf(0));
                    total.setAttributeNode(attr);
                }

                // Total Price elements
                if (!entry.getValue().getProductOrders().isEmpty()) {
                    Attr attr = doc.createAttribute("Price");
                    attr.setValue(entry.getValue().getTotalPrice().toString());
                    total.setAttributeNode(attr);
                } else {
                    Attr attr = doc.createAttribute("Price");
                    attr.setValue("EUR 0.00");
                    total.setAttributeNode(attr);
                }
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("Sales.xml"));

            transformer.transform(source, result);

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }

        return "redirect:/orders";
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
        GSOrder order = orderRepo.findOne(reclaimId).get();
        if (accept) {
            order.pay();
            orderRepo.save(order);

//            for (OrderLine line : order.getOrderLines()){
//                Quantity quantity = line.getQuantity();
//                GSInventoryItem item = inventory.findByProductIdentifier(line.getProductIdentifier()).get();
//                item.increaseQuantity(quantity);
//                inventory.save(item);
//            }

        } else {

            order.cancel();
            orderRepo.save(order);
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
        model.addAttribute("inventory", inventory);
        model.addAttribute("supercategories", superCategoryRepo.findAll());
        return "range";

    }

    @RequestMapping(value = "/range/delsuper", method = RequestMethod.DELETE)
    public String delSuper(@RequestParam("superName") String superName) {

        SuperCategory superCategory = superCategoryRepo.findByName(superName);

//        List<SubCategory> sub = superCategory.getSubCategories();


        while (!superCategory.getSubCategories().isEmpty()) {

            SubCategory subCategory = superCategory.getSubCategories().get(superCategory.getSubCategories().indexOf(superCategory.getSubCategories().get(0)));
            superCategory.getSubCategories().remove(subCategory);
            delSub(subCategory);

        }
        superCategoryRepo.save(superCategory);
        superCategoryRepo.delete(superCategory.getId());
        return "redirect:/range";
    }

    @RequestMapping(value = "/range/delsub", method = RequestMethod.DELETE)
    public String delSubRequest(@RequestParam("subName") String subName) {

        SubCategory subCategory = subCategoryRepo.findByName(subName);

        SuperCategory superCategory = subCategory.getSuperCategory();

//        superCategory.getSubCategories().remove(subCategory);


        delSub(subCategory);


        return "redirect:/range";
    }

    @RequestMapping(value = "/range/delproduct", method = RequestMethod.DELETE)
    public String delProductRequest(@RequestParam("productIdent") ProductIdentifier productIdentifier) {
        GSProduct product = catalog.findOne(productIdentifier).get();
        product.getSubCategory().getProducts().remove(product.getSubCategory().getProducts().indexOf(product));
        subCategoryRepo.save(product.getSubCategory());
        delProduct(productIdentifier);

        return "redirect:/range";
    }

    private void delSub(SubCategory subCategory) {
        while (!subCategory.getProducts().isEmpty()) {
            GSProduct product = subCategory.getProducts().get(0);
            ProductIdentifier productIdentifier = product.getIdentifier();
            subCategory.getProducts().remove(product);
            delProduct(productIdentifier);
            subCategoryRepo.save(subCategory);
        }
        Long id = subCategory.getId();
        subCategory.getSuperCategory().getSubCategories().remove(subCategory);
        superCategoryRepo.save(subCategory.getSuperCategory());
        subCategoryRepo.save(subCategory);
        subCategoryRepo.delete(id);

    }

    private void delProduct(ProductIdentifier productIdentifier) {
        GSProduct product = catalog.findOne(productIdentifier).get();
        product.setInRange(false);
        product.setSubCategory(null);
        Quantity quantity = Units.of(-1L);
        GSInventoryItem item = inventory.findByProductIdentifier(productIdentifier).get();
        item.setMinimalQuantity(quantity);
        item.decreaseQuantity(item.getQuantity());
        catalog.save(product);
        inventory.save(item);
    }

    @RequestMapping(value = "/range/editproduct/{prodId}")
    public String editProduct(Model model, @PathVariable("prodId") ProductIdentifier productId) {

        GSProduct product = catalog.findOne(productId).get();

        model.addAttribute("superCategory", superCategoryRepo.findAll());
        model.addAttribute("superCategories", superCategoryRepo.findAll());
        model.addAttribute("product", product);
        model.addAttribute("isNew", false);


        return "/editproduct";

    }

    @RequestMapping(value = "/range/editproduct", method = RequestMethod.POST)
    public String editProduct(@RequestParam("productName") String productName, @RequestParam("price") String strPrice,
                              @RequestParam("subCategory") String strSubcategory,
                              @RequestParam("productId") ProductIdentifier productId) {

        SubCategory subCategory = subCategoryRepo.findByName(strSubcategory);

        float price = Float.parseFloat(strPrice.substring(0, strPrice.indexOf(" ")));
        GSProduct product = catalog.findOne(productId).get();
        product.setSubCategory(subCategory);
        product.setName(productName);
        product.setPrice(Money.of(EUR, Math.round(price * 100) / 100.0));

        catalog.save(product);


        return "redirect:/range";

    }

    @RequestMapping(value = "/range/addproduct")
    public String addProduct(Model model) {

        model.addAttribute("superCategories", superCategoryRepo.findAll());
        model.addAttribute("isNew", true);
        return "/editproduct";
    }


}
