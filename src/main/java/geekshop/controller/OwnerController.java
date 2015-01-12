package geekshop.controller;

import geekshop.model.*;
import geekshop.model.validation.ProductForm;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.ProductIdentifier;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.order.OrderIdentifier;
import org.salespointframework.order.OrderLine;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.quantity.Units;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.validation.Valid;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * A Spring MVC controller to manage the shop owner's functions.
 *
 * @author Felix Döring
 * @author Sebastian Döring
 * @author Dominik Lauck
 */

@Controller
@PreAuthorize("hasRole('ROLE_OWNER')")
class OwnerController {
    private final GSOrderRepository orderRepo;
    private final Catalog<GSProduct> catalog;
    private final UserRepository userRepo;
    private final JokeRepository jokeRepo;
    private final MessageRepository messageRepo;
    private final SubCategoryRepository subCategoryRepo;
    private final SuperCategoryRepository superCategoryRepo;
    private final Inventory<GSInventoryItem> inventory;


    @Autowired
    public OwnerController(GSOrderRepository orderRepo, Catalog<GSProduct> catalog,
                           UserRepository userRepo, JokeRepository jokeRepo,
                           MessageRepository messageRepo, SubCategoryRepository subCategoryRepo,
                           SuperCategoryRepository superCategoryRepo, Inventory<GSInventoryItem> inventory) {
        this.orderRepo = orderRepo;
        this.catalog = catalog;
        this.userRepo = userRepo;
        this.jokeRepo = jokeRepo;
        this.messageRepo = messageRepo;
        this.subCategoryRepo = subCategoryRepo;
        this.superCategoryRepo = superCategoryRepo;
        this.inventory = inventory;
    }


    @RequestMapping("/orders")
    public String orders(Model model, @RequestParam(value = "sort", required = false) String sort) {

        if (sort != null && sort.equals("products")) {
            TreeMap<GSProduct, GSProductOrders> map = putMap();
            model.addAttribute("mapProductOrders", map);
        } else {
            TreeSet<GSOrder> orders = new TreeSet<>();
            for (GSOrder order : orderRepo.findAll()) {
                if (!order.isOpen() && !order.isCanceled()) { // open and canceled orders ought not to be shown
                    orders.add(order);
                }
            }
            model.addAttribute("setOrders", orders);
            model.addAttribute("userRepo", userRepo);
            model.addAttribute("catalog", catalog);
        }

        return "orders";
    }

    public TreeMap<GSProduct, GSProductOrders> putMap() {

        TreeMap<GSProduct, GSProductOrders> map = new TreeMap<GSProduct, GSProductOrders>();

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
            GSProductOrder productOrder = new GSProductOrder((GSOrderLine) ol, date, order.getOrderNumber(), order.getPaymentMethod(), seller);
            GSProduct product = catalog.findOne(ol.getProductIdentifier()).get();
            GSProductOrders prodOrders = map.get(product);
            if (prodOrders != null)
                prodOrders.addProductOrder(productOrder);
        }
    }

    @RequestMapping("/exportxml")
    public String exportXML(@RequestParam(value = "sort", required = false) String sort) {

        if (sort != null && sort.equals("products")) {
            createXMLSortedByProducts();
            return "redirect:/orders?sort=products";
        } else {
            createXMLSortedByOrders();
            return "redirect:/orders?sort=orders";
        }
    }

    public void createXMLSortedByProducts() {
        Map<GSProduct, GSProductOrders> map = putMap();

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            // root element
            Element rootElement = doc.createElement("orders");
            doc.appendChild(rootElement);

            for (Map.Entry<GSProduct, GSProductOrders> entry : map.entrySet()) {
                // product elements
                Element product = doc.createElement("product");
                product.setAttribute("name", entry.getKey().getName());
                product.setAttribute("productnr", Long.toString(entry.getKey().getProductNumber()));
                product.setAttribute("totalquantity", entry.getValue().getTotalQuantity().getAmount().toString());
                product.setAttribute("totalprice", entry.getValue().getTotalPrice().toString());
                rootElement.appendChild(product);

                for (GSProductOrder element : entry.getValue().getProductOrders()) {

                    // order elements
                    Element order = doc.createElement("order");
                    order.setAttribute("type", element.getOrderLine().getType().toString().toLowerCase());
                    product.appendChild(order);


                    // date element
                    Element date = doc.createElement("date");
                    date.setTextContent(element.getDate().toString());
                    order.appendChild(date);

                    // ordernumber element
                    Element orderNr = doc.createElement("ordernr");
                    orderNr.setTextContent(String.valueOf(element.getOrderNumber()));
                    order.appendChild(orderNr);

                    if (element.getOrderLine().getType() == OrderType.NORMAL) {
                        // payment method element
                        Element paymentmethod = doc.createElement("paymenttype");
                        paymentmethod.setTextContent(element.getPaymentType().toString().toLowerCase());
                        order.appendChild(paymentmethod);
                    }

                    // seller elements
                    Element seller = doc.createElement("seller");
                    seller.setTextContent(element.getSeller().toString());
                    order.appendChild(seller);

                    // quantity elements
                    Element quantity = doc.createElement("quantity");
                    quantity.appendChild(doc.createTextNode(element.getOrderLine().getQuantity().getAmount().toString()));
                    order.appendChild(quantity);

                    // price elements
                    Element price = doc.createElement("price");
                    price.setTextContent(element.getOrderLine().getPrice().toString());
                    order.appendChild(price);
                }
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("orders.xml"));

            transformer.transform(source, result);

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

    public void createXMLSortedByOrders() {
        TreeSet<GSOrder> orders = new TreeSet<>();
        for (GSOrder order : orderRepo.findAll()) {
            if (!order.isOpen() && !order.isCanceled()) { // open and canceled orders ought not to be shown
                orders.add(order);
            }
        }

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            // root element
            Element rootElement = doc.createElement("orders");
            doc.appendChild(rootElement);

            for (GSOrder o : orders) {
                // order elements
                Element order = doc.createElement("order");
                order.setAttribute("ordernr", String.valueOf(o.getOrderNumber()));
                order.setAttribute("type", o.getOrderType().toString().toLowerCase());
                order.setAttribute("date", o.getCreationDate().toString());
                if (o.getOrderType() == OrderType.RECLAIM) {
                    order.setAttribute("reclaimedorder", String.valueOf(o.getReclaimedOrder().getOrderNumber()));
                } else {
                    order.setAttribute("paymenttype", o.getPaymentType().toString().toLowerCase());
                }
                order.setAttribute("seller", userRepo.findByUserAccount(o.getUserAccount()).toString());
                order.setAttribute("totalprice", o.getTotalPrice().toString());
                rootElement.appendChild(order);

                for (OrderLine ol : o.getOrderLines()) {

                    // product elements
                    Element product = doc.createElement("product");
                    order.appendChild(product);


                    // name element
                    Element name = doc.createElement("name");
                    name.setTextContent(ol.getProductName());
                    product.appendChild(name);

                    // productnumber element
                    Element productNr = doc.createElement("productnr");
                    productNr.setTextContent(String.valueOf(catalog.findOne(ol.getProductIdentifier()).get().getProductNumber()));
                    product.appendChild(productNr);

                    // quantity element
                    Element quantity = doc.createElement("quantity");
                    quantity.setTextContent(ol.getQuantity().getAmount().toString());
                    product.appendChild(quantity);

                    // price elements
                    Element price = doc.createElement("price");
                    price.setTextContent(ol.getPrice().toString());
                    product.appendChild(price);
                }
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("orders.xml"));

            transformer.transform(source, result);

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

    /**
     * Shows an open {@link geekshop.model.GSOrder} with {@link geekshop.model.OrderType} RECLAIM.
     *
     * @param reclaimId the OrderIdentifier from the Order
     * @param msgId     the Id of the Message
     * @return
     */
    @RequestMapping(value = "/showreclaim/{rid}", method = RequestMethod.POST)
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

    /**
     * Accepts or declines the open {@link geekshop.model.GSOrder} with {@link geekshop.model.OrderType} RECLAIM and close it.
     * Deletes the message.
     *
     * @param reclaimId the OrderIdentifier from the Order
     * @param msgId     the Id of the Message
     * @param accept    boolean value if the reclaim is accepted
     * @return
     */
    @RequestMapping(value = "/showreclaim/{rid}", method = RequestMethod.DELETE)
    public String acceptReclaim(@PathVariable("rid") OrderIdentifier reclaimId, @RequestParam("msgId") Long msgId, @RequestParam("accept") Boolean accept) {
        messageRepo.delete(msgId);
        GSOrder order = orderRepo.findOne(reclaimId).get();
        if (accept) {
            order.pay();
            orderRepo.save(order);
        } else {

            order.cancel();
            orderRepo.save(order);
        }

        return "redirect:/messages";
    }


    /**
     * Shows all existing {@link geekshop.model.Joke}s.
     *
     * @return
     */
    @RequestMapping("/jokes")
    public String jokes(Model model) {
        model.addAttribute("jokes", jokeRepo.findAll());
        return "jokes";
    }

    /**
     * Shows editpage for new {@link geekshop.model.Joke}s.
     *
     * @return
     */
    @RequestMapping("/newjoke")
    public String newJoke() {
        return "editjoke";
    }

    /**
     * Creates a new instance of {@link geekshop.model.Joke} and save it to the {@link geekshop.model.JokeRepository}.
     *
     * @param text the text of the Joke
     * @return
     */
    @RequestMapping(value = "/newjoke", method = RequestMethod.POST)
    public String newJoke(@RequestParam("jokeText") String text) {
        if (!text.trim().isEmpty())
            jokeRepo.save(new Joke(text));
        return "redirect:/jokes";
    }


    /**
     * Shows editpage for an existing {@link geekshop.model.Joke}.
     *
     * @param id the Id of the Joke
     * @return
     */
    @RequestMapping("/jokes/{id}")
    public String showJoke(Model model, @PathVariable("id") Long id) {
        Joke joke = jokeRepo.findById(id);
        model.addAttribute("joke", joke);
        return "editjoke";
    }


    /**
     * Edits an existing {@link geekshop.model.Joke} and save it to the {@link geekshop.model.JokeRepository}.
     *
     * @param id       the Id of the Joke
     * @param jokeText the text of the Joke
     * @return
     */
    @RequestMapping(value = "/editjoke/{id}", method = RequestMethod.POST)
    public String editJoke(@PathVariable("id") Long id, @RequestParam("jokeText") String jokeText) {
        if (!jokeText.trim().isEmpty()) {
            Joke joke = jokeRepo.findById(id);
            joke.setText(jokeText);
            jokeRepo.save(joke);
        }
        return "redirect:/jokes";
    }


    /**
     * Deletes an existing {@link geekshop.model.Joke} from the {@link geekshop.model.JokeRepository}.
     *
     * @param id the Id of the Joke
     * @return
     */
    @RequestMapping(value = "/jokes/{id}", method = RequestMethod.DELETE)
    public String deleteJoke(@PathVariable("id") Long id) {
        Joke joke = jokeRepo.findById(id);
        Iterable<User> allUsers = userRepo.findAll();
        for (User user : allUsers) {
            List<Joke> recentJokes = user.getRecentJokes();
            recentJokes.removeAll(Collections.singletonList(joke));
            userRepo.save(user);
        }

        jokeRepo.delete(joke);
        return "redirect:/jokes";
    }


    /**
     * Deletes all existing {@link geekshop.model.Joke}s.
     *
     * @return
     */
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


    /**
     * Shows all existing {@link geekshop.model.Message}s.
     *
     * @return
     */
    @RequestMapping("/messages")
    public String messages(Model model) {
        model.addAttribute("ownermessage", messageRepo.findAll());
        return "messages";
    }

    /**
     * Deletes an existing {@link geekshop.model.Message} from the {@link geekshop.model.MessageRepository}.
     *
     * @param id the Id of the Message
     * @return
     */
    @RequestMapping(value = "/messages/{id}", method = RequestMethod.DELETE)
    public String deleteMessage(@PathVariable("id") Long id) {
        messageRepo.delete(id);
        return "redirect:/messages";
    }


    /**
     * Shows all existing {@link geekshop.model.SuperCategory}s, {@link geekshop.model.SubCategory}s an {@link geekshop.model.GSProduct}s.
     *
     * @param model
     * @return
     */
    @RequestMapping("/range")
    public String range(Model model) {
        model.addAttribute("inventory", inventory);
        model.addAttribute("supercategories", superCategoryRepo.findAll());
        return "range";

    }


    /**
     * Deletes a {@link geekshop.model.SuperCategory}.
     *
     * @param superName the name of the SuperCategory
     * @return
     */
    @RequestMapping(value = "/range/delsuper", method = RequestMethod.DELETE)
    public String delSuper(@RequestParam("superName") String superName) {

        SuperCategory superCategory = superCategoryRepo.findByName(superName);


        while (!superCategory.getSubCategories().isEmpty()) {

            SubCategory subCategory = superCategory.getSubCategories().get(superCategory.getSubCategories().indexOf(superCategory.getSubCategories().get(0)));
            superCategory.getSubCategories().remove(subCategory);
            delSub(subCategory);

        }
        superCategoryRepo.save(superCategory);
        superCategoryRepo.delete(superCategory.getId());
        return "redirect:/range";
    }

    /**
     * Deletes a {@link geekshop.model.SubCategory} when requested.
     *
     * @param subName the name of the SubCategory
     * @return
     */
    @RequestMapping(value = "/range/delsub", method = RequestMethod.DELETE)
    public String delSubRequest(@RequestParam("subName") String subName) {

        SubCategory subCategory = subCategoryRepo.findByName(subName);

//        SuperCategory superCategory = subCategory.getSuperCategory();

//        superCategory.getSubCategories().remove(subCategory);


        delSub(subCategory);


        return "redirect:/range";
    }

    /**
     * Deletes a {@link geekshop.model.GSProduct} when requested.
     *
     * @param productIdentifier the ProductIdentifier
     * @return
     */
    @RequestMapping(value = "/range/delproduct", method = RequestMethod.DELETE)
    public String delProductRequest(@RequestParam("productIdent") ProductIdentifier productIdentifier) {
        GSProduct product = catalog.findOne(productIdentifier).get();
        product.getSubCategory().getProducts().remove(product.getSubCategory().getProducts().indexOf(product));
        subCategoryRepo.save(product.getSubCategory());
        delProduct(productIdentifier);

        return "redirect:/range";
    }

    /**
     * Deletes a {@link geekshop.model.SubCategory} from {@link geekshop.model.SubCategoryRepository}.
     *
     * @param subCategory the SubCategory to be deleted
     */
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

    /**
     * Deletes a {@link geekshop.model.GSProduct}.
     *
     * @param productIdentifier the ProductIdentiefert of the GSProduct to be deleted
     */
    private void delProduct(ProductIdentifier productIdentifier) {
        GSProduct product = catalog.findOne(productIdentifier).get();
        product.setInRange(false);
        product.setSubCategory(null);
        Quantity quantity = Units.ZERO;
        GSInventoryItem item = inventory.findByProductIdentifier(productIdentifier).get();
        item.setMinimalQuantity(quantity);
        item.decreaseQuantity(item.getQuantity());
        catalog.save(product);
        inventory.save(item);
    }


    /**
     * Shows Editpage for the {@link geekshop.model.GSProduct}.
     *
     * @param productId the ProductIdentifier of the GSProduct to be edited
     * @return
     */
    @RequestMapping("/range/editproduct/{prodId}")
    public String editProduct(Model model, @PathVariable("prodId") ProductIdentifier productId) {

        GSProduct product = catalog.findOne(productId).get();
        GSInventoryItem item = inventory.findByProduct(product).get();

        model.addAttribute("productForm", new ProductForm(
                product.getName(), String.valueOf(product.getProductNumber()), GSProduct.moneyToString(product.getPrice(), true),
                String.valueOf(item.getMinimalQuantity().getAmount().longValue()), String.valueOf(item.getQuantity().getAmount().longValue()), product.getSubCategory().getName()
        ));
        model.addAttribute("productName", product.getName());
        model.addAttribute("superCategories", superCategoryRepo.findAll());
        model.addAttribute("isNew", false);

        return "editproduct";

    }

    /**
     * Save the changes on {@link geekshop.model.GSProduct} to {@link org.salespointframework.inventory.Inventory}.
     *
     * @param productId   the ProductIdentifier of the GSProduct
     * @param productForm contains the form data
     * @param result      the result of validation
     * @return
     */
    @RequestMapping(value = "/range/editproduct/{prodId}", method = RequestMethod.POST)
    public String editProduct(Model model, @PathVariable("prodId") ProductIdentifier productId,
                              @ModelAttribute("productForm") @Valid ProductForm productForm, BindingResult result) {

        GSProduct product = catalog.findOne(productId).get();

        for (GSProduct p : catalog.findAll()) {
            if (!p.equals(product) && p.getProductNumber() == Long.parseLong(productForm.getProductNumber())) {
                result.addError(new FieldError("productForm", "productNumber", "Diese Artikelnummer ist bereits vergeben."));
                break;
            }
        }
        if (result.hasErrors()) {
            model.addAttribute("productForm", productForm);
            model.addAttribute("productName", product.getName());
            model.addAttribute("superCategories", superCategoryRepo.findAll());
            model.addAttribute("isNew", false);
            return "editproduct";
        }

        product.setName(productForm.getName());

        SubCategory subCategory_new = subCategoryRepo.findByName(productForm.getSubCategory());
        SubCategory subCategory_old = product.getSubCategory();
        subCategory_old.getProducts().remove(product);
        subCategoryRepo.save(subCategory_old);
        subCategory_new.getProducts().add(product);
        subCategoryRepo.save(subCategory_new);
        product.setSubCategory(subCategory_new);

        String strPrice = productForm.getPrice();
        strPrice = strPrice.substring(0, strPrice.contains(" ") ? strPrice.lastIndexOf(" ") : strPrice.length());
        strPrice = strPrice.replaceAll("\\.", "");
        strPrice = strPrice.replaceAll(",", ".");
        Money price = Money.of(CurrencyUnit.EUR, Double.parseDouble(strPrice));
        product.setPrice(price);

        catalog.save(product);

        GSInventoryItem item = inventory.findByProductIdentifier(productId).get();
        Quantity setQuantity = Units.of(Long.parseLong(productForm.getQuantity())).subtract(item.getQuantity());
        item.increaseQuantity(setQuantity);
        item.setMinimalQuantity(Units.of(Long.parseLong(productForm.getMinQuantity())));
        inventory.save(item);

        if (!item.hasSufficientQuantity()) {
            messageRepo.save(new Message(MessageKind.NOTIFICATION,
                    "Die verfügbare Menge des Artikels „" + item.getProduct().getName() + "“ " +
                            "(Artikelnr. " + GSOrder.longToString(((GSProduct) item.getProduct()).getProductNumber()) +
                            ") hat mit " + item.getQuantity().getAmount() + " Stück " +
                            "die festgelegte Mindestanzahl von " + item.getMinimalQuantity().getAmount() +
                            " Stück unterschritten."));
        }


        return "redirect:/range";

    }

    /**
     * Shows Editpage to create new {@link geekshop.model.GSProduct}.
     *
     * @return
     */
    @RequestMapping(value = "/range/addproduct")
    public String addProduct(Model model) {

        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("superCategories", superCategoryRepo.findAll());
        model.addAttribute("isNew", true);

        return "editproduct";
    }

    /**
     * Creates a new instance of {@link geekshop.model.GSProduct} and save it to {@link org.salespointframework.inventory.Inventory}.
     *
     * @param productForm contains the form data
     * @param result      the result of validation
     * @return
     */
    @RequestMapping(value = "/range/addproduct", method = RequestMethod.POST)
    public String addProductToCatalog(Model model, @ModelAttribute("productForm") @Valid ProductForm productForm, BindingResult result) {

        for (GSProduct p : catalog.findAll()) {
            if (productForm.getProductNumber() != null && !productForm.getProductNumber().isEmpty() && p.getProductNumber() == Long.parseLong(productForm.getProductNumber())) {
                result.addError(new FieldError("productForm", "productNumber", "Diese Artikelnummer ist bereits vergeben."));
                break;
            }
        }
        if (result.hasErrors()) {
            model.addAttribute("productForm", productForm);
            model.addAttribute("superCategories", superCategoryRepo.findAll());
            model.addAttribute("isNew", true);
            return "editproduct";
        }


        Quantity quantity = Units.of(Long.parseLong(productForm.getQuantity()));
        Quantity minQuantity = Units.of(Long.parseLong(productForm.getMinQuantity()));

        SubCategory subCategory = subCategoryRepo.findByName(productForm.getSubCategory());
        String strPrice = productForm.getPrice();
        strPrice = strPrice.substring(0, strPrice.contains(" ") ? strPrice.lastIndexOf(" ") : strPrice.length());
        strPrice = strPrice.replaceAll("\\.", "");
        strPrice = strPrice.replaceAll(",", ".");
        Money price = Money.of(CurrencyUnit.EUR, Double.parseDouble(strPrice));
        GSProduct product = new GSProduct(Long.parseLong(productForm.getProductNumber()), productForm.getName(), price, subCategory);
        catalog.save(product);

        subCategory.addProduct(product);
        subCategoryRepo.save(subCategory);

        GSInventoryItem item = new GSInventoryItem(product, quantity, minQuantity);
        inventory.save(item);

        return "redirect:/range";
    }


    /**
     * Shows the Editpage for {@link geekshop.model.SuperCategory}s.
     *
     * @param superCatName the name of the SuperCategory
     * @return
     */
    @RequestMapping(value = "/range/editsuper/{super}")
    public String editSuperCategory(Model model, @PathVariable("super") String superCatName) {

        if (superCategoryRepo.findByName(superCatName) == null)
            return "redirect:/range";

        SuperCategory superCategory = superCategoryRepo.findByName(superCatName);
        model.addAttribute("super", superCategory);
        model.addAttribute("name", superCatName);

        return "editsuper";

    }


    /**
     * Saves changes from a {@link geekshop.model.SuperCategory} to {@link geekshop.model.SuperCategoryRepository}.
     *
     * @param superCatName the old name of the Supercategory
     * @param name         the new name of the Supercategory
     * @return
     */
    @RequestMapping(value = "/range/editsuper/{super}", method = RequestMethod.POST)
    public String editSuper(Model model, @PathVariable("super") String superCatName, @RequestParam("name") String name) {

        if (superCategoryRepo.findByName(superCatName) == null)
            return "redirect:/range";

        SuperCategory superCategory = superCategoryRepo.findByName(superCatName);

        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("super", superCategory);
            model.addAttribute("name", name);
            model.addAttribute("nameError", "Der Name der Kategorie darf nicht leer sein.");
            return "editsuper";
        }

        if (!superCatName.equals(name) && superCategoryRepo.findByName(name.trim()) != null) {
            model.addAttribute("super", superCategory);
            model.addAttribute("name", name);
            model.addAttribute("nameError", "Dieser Name ist bereits vergeben.");
            return "editsuper";
        }

        superCategory.setName(name.trim());
        superCategoryRepo.save(superCategory);

        return "redirect:/range";

    }

    /**
     * Shows Editpage to create new {@link geekshop.model.SuperCategory}.
     *
     * @return
     */
    @RequestMapping(value = "/range/addsuper")
    public String addSuper(Model model) {
        return "editsuper";
    }


    /**
     * Creates a new instance of {@link geekshop.model.SuperCategory} an saves it to {@link geekshop.model.SuperCategoryRepository}.
     *
     * @param name the name of the Supercategory
     * @return
     */
    @RequestMapping(value = "/range/addsuper", method = RequestMethod.POST)
    public String addSuperCategory(Model model, @RequestParam("name") String name) {

        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("name", name);
            model.addAttribute("nameError", "Der Name der Kategorie darf nicht leer sein.");
            return "editsuper";
        }

        if (superCategoryRepo.findByName(name.trim()) != null) {
            model.addAttribute("name", name);
            model.addAttribute("nameError", "Diese Kategorie existiert bereits.");
            return "editsuper";
        }

        SuperCategory superCategory = new SuperCategory(name.trim());
        superCategoryRepo.save(superCategory);


        return "redirect:/range";
    }


    /**
     * Shows the Editpage for {@link geekshop.model.SubCategory}s.
     *
     * @param subCatName the name of the Subcategory
     * @return
     */
    @RequestMapping(value = "/range/editsub/{sub}")
    public String editSubCategory(Model model, @PathVariable("sub") String subCatName) {

        if (subCategoryRepo.findByName(subCatName) == null)
            return "redirect:/range";

        SubCategory subCategory = subCategoryRepo.findByName(subCatName);
        model.addAttribute("sub", subCategory);
        model.addAttribute("superCategory", subCategory.getSuperCategory().getName());
        model.addAttribute("name", subCatName);
        model.addAttribute("superCategories", superCategoryRepo.findAll());

        return "editsub";

    }

    /**
     * Saves changes from a {@link geekshop.model.SubCategory} to {@link geekshop.model.SubCategoryRepository}.
     *
     * @param subCatName  the old name of the SubCategory
     * @param name        the new name of the SubCategory
     * @param strSuperCat the name of the (new) SuperCategory
     * @return
     */
    @RequestMapping(value = "/range/editsub/{sub}", method = RequestMethod.POST)
    public String editSub(Model model, @PathVariable("sub") String subCatName, @RequestParam("name") String name, @RequestParam(value = "superCategory", required = false) String strSuperCat) {

        if (subCategoryRepo.findByName(subCatName) == null)
            return "redirect:/range";

        SubCategory subCategory = subCategoryRepo.findByName(subCatName);

        boolean hasErrors = false;

        if (strSuperCat == null || strSuperCat.trim().isEmpty()) {
            model.addAttribute("superCatError", "Es wurde keine Oberkategorie ausgewählt.");
            hasErrors = true;
        }

        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("nameError", "Der Name der Kategorie darf nicht leer sein.");
            hasErrors = true;
        } else if (!subCatName.equals(name.trim()) && subCategoryRepo.findByName(name.trim()) != null) {
            model.addAttribute("nameError", "Dieser Name ist bereits vergeben.");
            hasErrors = true;
        }

        if (hasErrors) {
            model.addAttribute("sub", subCategory);
            model.addAttribute("superCategory", strSuperCat);
            model.addAttribute("name", name);
            model.addAttribute("superCategories", superCategoryRepo.findAll());
            return "editsub";
        }

        SuperCategory superCategory_new = superCategoryRepo.findByName(strSuperCat.trim());
        SuperCategory superCategory_old = subCategory.getSuperCategory();

        subCategory.setName(name.trim());
        subCategory.setSuperCategory(superCategory_new);

        superCategory_new.addSubCategory(subCategory);
        superCategory_old.getSubCategories().remove(subCategory);

        superCategoryRepo.save(superCategory_old);
        superCategoryRepo.save(superCategory_new);


        subCategoryRepo.save(subCategory);

        return "redirect:/range";

    }


    /**
     * Shows Editpage to create new {@link geekshop.model.SubCategory}.
     *
     * @param name        the name of the SubCategory
     * @param strSuperCat the name of the SuperCategory
     * @return
     */
    @RequestMapping(value = "/range/addsub", method = RequestMethod.POST)
    public String addSubCategory(Model model, @RequestParam("name") String name, @RequestParam(value = "superCategory", required = false) String strSuperCat) {

        boolean hasErrors = false;

        if (strSuperCat == null || strSuperCat.trim().isEmpty()) {
            model.addAttribute("superCatError", "Es wurde keine Oberkategorie ausgewählt.");
            hasErrors = true;
        }

        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("nameError", "Der Name der Kategorie darf nicht leer sein.");
            hasErrors = true;
        } else if (subCategoryRepo.findByName(name.trim()) != null) {
            model.addAttribute("nameError", "Diese Kategorie existiert bereits.");
            hasErrors = true;
        }

        if (hasErrors) {
            model.addAttribute("superCategory", strSuperCat);
            model.addAttribute("name", name);
            model.addAttribute("superCategories", superCategoryRepo.findAll());
            return "editsub";
        }

        SuperCategory superCategory = superCategoryRepo.findByName(strSuperCat.trim());

        SubCategory subCategory = new SubCategory(name.trim(), superCategory);
        superCategory.addSubCategory(subCategory);
        subCategoryRepo.save(subCategory);
        superCategoryRepo.save(superCategory);

        return "redirect:/range";
    }


    /**
     * Creates a new instance of {@link geekshop.model.SubCategory} and saves it to {@link geekshop.model.SubCategoryRepository}.
     *
     * @return
     */
    @RequestMapping(value = "/range/addsub")
    public String addSub(Model model) {

        model.addAttribute("superCategories", superCategoryRepo.findAll());
        return "editsub";
    }


}
