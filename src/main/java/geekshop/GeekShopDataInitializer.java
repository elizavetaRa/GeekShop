package geekshop;

import geekshop.model.*;
import org.joda.money.Money;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.core.DataInitializer;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.order.OrderIdentifier;
import org.salespointframework.order.OrderLine;
import org.salespointframework.order.OrderManager;
import org.salespointframework.payment.Cash;
import org.salespointframework.quantity.Units;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Calendar;

import static org.joda.money.CurrencyUnit.EUR;

/**
 * A {@link DataInitializer} implementation that will create dummy data for the application on application startup.
 *
 * @author Felix D&ouml;ring
 * @author Sebastian D&ouml;ring
 */

@Component
public class GeekShopDataInitializer implements DataInitializer {

    private final Catalog<GSProduct> catalog;
    private final Inventory<GSInventoryItem> inventory;
    private final JokeRepository jokeRepo;
    private final MessageRepository messageRepo;
    private final PasswordRulesRepository passRulesRepo;
    private final SubCategoryRepository subCatRepo;
    private final SuperCategoryRepository supCatRepo;
    private final UserAccountManager userAccountManager;
    private final UserRepository userRepo;
    private final OrderManager<GSOrder> orderManager; // nur zu Testzwecken
    private final GSOrderRepository orderRepo; // nur zu Testzwecken

    @Autowired
    public GeekShopDataInitializer(Catalog<GSProduct> catalog, Inventory<GSInventoryItem> inventory, JokeRepository jokeRepo,
                                   MessageRepository messageRepo, PasswordRulesRepository passRulesRepo, SubCategoryRepository subCatRepo,
                                   SuperCategoryRepository supCatRepo, UserAccountManager userAccountManager, UserRepository userRepo,
                                   OrderManager<GSOrder> orderManager, GSOrderRepository orderRepo) {

        Assert.notNull(catalog, "Catalog must not be null!");
        Assert.notNull(inventory, "Inventory must not be null!");
        Assert.notNull(jokeRepo, "JokeRepository must not be null!");
        Assert.notNull(messageRepo, "MessageRepository must not be null!");
        Assert.notNull(passRulesRepo, "PasswordRulesRepository must not be null!");
        Assert.notNull(subCatRepo, "SubCategoryRepository must not be null!");
        Assert.notNull(supCatRepo, "SuperCategoryRepository must not be null!");
        Assert.notNull(userAccountManager, "UserAccountManager must not be null!");
        Assert.notNull(userRepo, "UserRepository must not be null!");

        this.catalog = catalog;
        this.inventory = inventory;
        this.jokeRepo = jokeRepo;
        this.messageRepo = messageRepo;
        this.passRulesRepo = passRulesRepo;
        this.subCatRepo = subCatRepo;
        this.supCatRepo = supCatRepo;
        this.userAccountManager = userAccountManager;
        this.userRepo = userRepo;
        this.orderManager = orderManager;
        this.orderRepo = orderRepo;

    }

    /**
     * Overriding method of {@link org.salespointframework.core.DataInitializer} triggering several initializing methods.
     */
    @Override
    public void initialize() {

        initializeCatalog();
        initializePasswordRules();
        initializeUsers();
        initializeJokes();
        initializeTestOrders(); // nur zu Testzwecken
        initializeMessages();
    }

    /**
     * Initializes {@link SuperCategory}s, {@link SubCategory}s and {@link GSProduct}s in {@link org.salespointframework.catalog.Catalog} as well as {@link GSInventoryItem}s in {@link Inventory}.
     */
    private void initializeCatalog() {

        // Skip creation if database was already populated
        if (catalog.findAll().iterator().hasNext())
            return;

        SuperCategory sup1 = new SuperCategory("SuperCategory1");
        SuperCategory sup2 = new SuperCategory("SuperCategory2");
        SuperCategory sup3 = new SuperCategory("SuperCategory3");
        SubCategory sub1 = new SubCategory("SubCategory1", sup1);
        SubCategory sub2 = new SubCategory("SubCategory2", sup1);
        SubCategory sub3 = new SubCategory("SubCategory3", sup2);
        SubCategory sub4 = new SubCategory("SubCategory4", sup2);
        sup1.addSubCategory(sub1);
        sup1.addSubCategory(sub2);
        sup2.addSubCategory(sub3);
        sup2.addSubCategory(sub4);
        supCatRepo.save(sup1);
        supCatRepo.save(sup2);
        supCatRepo.save(sup3);
        subCatRepo.save(sub1);
        subCatRepo.save(sub2);
        subCatRepo.save(sub3);
        subCatRepo.save(sub4);


        GSProduct prod1 = new GSProduct("Product1", Money.of(EUR, 9.99), sub1, 5);
        GSProduct prod2 = new GSProduct("Product2", Money.of(EUR, 19.99), sub2, 1);
        GSProduct prod3 = new GSProduct("Product3", Money.of(EUR, 29.99), sub2, 4);
        GSProduct prod4 = new GSProduct("Product4", Money.of(EUR, 39.99), sub1, 3);
        GSProduct prod5 = new GSProduct("Product5", Money.of(EUR, 49.99), sub3, 2);

        catalog.save(prod1);
        catalog.save(prod2);
        catalog.save(prod3);
        catalog.save(prod4);
        catalog.save(prod5);

        System.out.println(catalog.count());


        sub1.addProduct(prod1);
        sub2.addProduct(prod2);
        sub2.addProduct(prod3);
        sub1.addProduct(prod4);
        sub3.addProduct(prod5);


        for (GSProduct product : catalog.findAll()) {
            if (product.getClass().equals(GSProduct.class)) {
                GSInventoryItem inventoryItem = new GSInventoryItem(product, Units.TEN, Units.ONE);
                inventory.save(inventoryItem);
                System.out.println(product.getName() + ": " + product.getSubCategory());
            }
        }
    }

    /**
     * Initializes {@link PasswordRules} in {@link PasswordRulesRepository}.
     */
    private void initializePasswordRules() {
        if (passRulesRepo.count() > 0)
            return;

        passRulesRepo.save(new PasswordRules());
    }

    /**
     * Initializes {@link User}s in {@link UserRepository}.
     */
    private void initializeUsers() {

        if (userAccountManager.findByUsername("owner").isPresent())
            return;


        Calendar cal = Calendar.getInstance();
        cal.set(1900, 2, 14);

        String phone = "01231234567";
        String street = "Musterstrasse";
        String houseNr = "1";
        String postcode = "12345";
        String place = "Musterstadt";

        UserAccount ownerAccount = userAccountManager.create("owner", "123", new Role("ROLE_OWNER"));
        ownerAccount.setFirstname("Owner");
        ownerAccount.setLastname("");
        userAccountManager.save(ownerAccount);

        final Role employeeRole = new Role("ROLE_EMPLOYEE");

        UserAccount ua1 = userAccountManager.create("hans", "123", employeeRole);
        ua1.setFirstname("Hans");
        ua1.setLastname("Hinz");
        userAccountManager.save(ua1);
        UserAccount ua2 = userAccountManager.create("erna", "123", employeeRole);
        ua2.setFirstname("Erna");
        ua2.setLastname("Anre");
        userAccountManager.save(ua2);
        UserAccount ua3 = userAccountManager.create("earlhickey", "123", employeeRole);
        ua3.setFirstname("Earl");
        ua3.setLastname("Hickey");
        userAccountManager.save(ua3);
        UserAccount ua4 = userAccountManager.create("mclovinfogell", "123", employeeRole);
        ua4.setFirstname("Fogell");
        ua4.setLastname("McLovin");
        userAccountManager.save(ua4);


        User owner = new User(ownerAccount, "123", Gender.SOMETHING_ELSE, cal.getTime(), MaritalStatus.UNKNOWN,
                "123456789", "Ownerstreet", "0", "01234", "Ownercity");
        User u1 = new User(ua1, "123", Gender.MALE, cal.getTime(), MaritalStatus.UNMARRIED, phone, street, houseNr, postcode, place);
        u1.setPwHasToBeChanged(true);
        User u2 = new User(ua2, "123", Gender.FEMALE, cal.getTime(), MaritalStatus.MARRIED, phone, street, houseNr, postcode, place);
        u2.setPwHasToBeChanged(true);
        User u3 = new User(ua3, "123", Gender.SOMETHING_ELSE, cal.getTime(), MaritalStatus.DIVORCED, phone, street, houseNr, postcode, place);
        u3.setPwHasToBeChanged(true);
        User u4 = new User(ua4, "123", Gender.SOMETHING_ELSE, cal.getTime(), MaritalStatus.WIDOWED, phone, street, houseNr, postcode, place);
        u4.setPwHasToBeChanged(true);

        userRepo.save(Arrays.asList(owner, u1, u2, u3, u4));
    }

    /**
     * Initializes {@link Joke}s in {@link JokeRepository}.
     */
    private void initializeJokes() {
        if (jokeRepo.count() > 0)
            return;


        jokeRepo.save(new Joke(
                "Frau: „Ich habe das neuste Windows-System.“" + System.getProperty("line.separator") +
                        "Berater: „Ja, und?“" + System.getProperty("line.separator") +
                        "Frau: „Ich habe da ein Problem.“" + System.getProperty("line.separator") +
                        "Berater: „Ja, aber das haben Sie doch bereits gesagt.“"
        ));
        jokeRepo.save(new Joke(
                "Ein Informatiker stellt sich jeden Abend ein volles und ein leeres Glas Wasser neben sein Bett. Warum?" + System.getProperty("line.separator") +
                        "– Das volle Glas ist dafür da, falls er in der Nacht aufwacht und Durst hat." + System.getProperty("line.separator") +
                        "Und das leere Glas, falls er in der Nacht aufwacht und keinen Durst hat."
        ));
        jokeRepo.save(new Joke(
                "Was hat Windows mit einem U-Boot gemein?" + System.getProperty("line.separator") + "Kaum macht man ein Fenster auf, fangen die Probleme an."
        ));
        jokeRepo.save(new Joke(
                "Immer wenn jemand auf „Eigene Dateien“ klickt, fällt irgendwo ein NSA-Mitarbeiter lachend vom Stuhl."
        ));
        jokeRepo.save(new Joke(
                "Es gibt genau 10 Arten von Menschen: Die, die binäre Zahlen verstehen und die, die es nicht tun."
        ));
        jokeRepo.save(new Joke(
                "Treffen sich zufällig zwei Informatiker im Park. Der eine kommt auf einem Fahrrad daher." + System.getProperty("line.separator") +
                        "„Hey, cooles Rad. Wo hast du denn das her?“" + System.getProperty("line.separator") +
                        "„Ach, das ist eine seltsame Geschichte“, antwortet der andere. „Ich komme da hinten in den Park. " +
                        "Da kommt eine Frau in einem blauen Kleid auf dem Fahrrad daher, versperrt mir den Weg, " +
                        "zieht sich das Kleid aus und wirft es vor mir auf den Boden. Dann steht die da splitterfasernackt und meint zu mir: " +
                        "‚Du kannst von mir haben, was du willst!‘. Tja und da hab ich halt das Fahrrad genommen.“" + System.getProperty("line.separator") +
                        "„Ja, das war klug von dir“, antwortet der andere. „Im blauen Kleid hättest Du auch ziemlich bescheuert ausgesehen!“"
        ));
        jokeRepo.save(new Joke(
                "Treffen sich zwei Pointer auf dem Stack. Sagt der eine zum anderen: „Ey, hör auf, auf mich zu zeigen!“"
        ));
        jokeRepo.save(new Joke(
                "DAU: „Mein Monitor geht nicht.“" + System.getProperty("line.separator") +
                        "Helpdesk: „Ist er denn eingeschaltet?“" + System.getProperty("line.separator") +
                        "DAU: „Ja.“" + System.getProperty("line.separator") +
                        "Helpdesk: „Schalten Sie ihn doch bitte mal aus.“" + System.getProperty("line.separator") +
                        "DAU: „Ah, jetzt geht’s …“"
        ));
        jokeRepo.save(new Joke(
                "Dicker Nebel. Ein kleines amerikanisches Flugzeug hat sich verflogen. " +
                        "Der Pilot kreist um das oberste Stockwerk eines Bürohauses, lehnt sich aus dem Cockpit " +
                        "und brüllt durch ein offenes Fenster: „Wo sind wir?“" + System.getProperty("line.separator") +
                        "Ein Mann blickt von seinem PC auf: „In einem Flugzeug!“" + System.getProperty("line.separator") +
                        "Der Pilot dreht eine scharfe Kurve und landet fünf Minuten später " +
                        "mit dem letzten Tropfen Treibstoff auf dem Flughafen von Seattle." + System.getProperty("line.separator") +
                        "Die verblüfften Passagiere wollen wissen, wie der Pilot es geschafft habe, sich zu orientieren." + System.getProperty("line.separator") +
                        "„Ganz einfach“, sagt der Pilot. „Die Antwort auf meine Frage war kurz, korrekt und völlig nutzlos. " +
                        "Ich hatte also mit der Microsoft-Hotline gesprochen. " +
                        "Das Microsoft Gebäude liegt 5 Meilen westlich vom Flughafen Seattle, Kurs 89 Grad.“"
        ));
        jokeRepo.save(new Joke(
                "Am Straßenrand steht ein Auto mit einem platten Reifen. Woran erkennt man, dass der Fahrer Informatiker ist?" + System.getProperty("line.separator") +
                        "– Wenn er nachsieht, ob an den anderen Reifen der gleiche Fehler auftritt."
        ));
    }


    private void initializeTestOrders() { // nur zu Testzwecken

        if (orderManager.find(userAccountManager.findByUsername("owner").get()).iterator().hasNext())
            return;

        UserAccount ua = userAccountManager.findByUsername("owner").get(); // suche UserAccount von owner
        GSProduct prod1 = catalog.findByName("Product1").iterator().next(); // suche Product1 (siehe initializeCatalog)
        GSProduct prod2 = catalog.findByName("Product2").iterator().next(); // suche Product1 (siehe initializeCatalog)
        GSOrder order1 = new GSOrder("1", ua, Cash.CASH); // erzeuge GSOrder
        GSOrder order2 = new GSOrder("2", ua, Cash.CASH, order1); // erzeuge Reclaim-GSOrder
        GSOrder order3 = new GSOrder("3", ua, Cash.CASH);
        GSOrder order4 = new GSOrder("4", ua, Cash.CASH, order1);
        GSOrderLine orderLine11 = new GSOrderLine(prod1, Units.TEN);
        GSOrderLine orderLine12 = new GSOrderLine(prod2, Units.TEN);
        GSOrderLine orderLine21 = new GSOrderLine(prod2, Units.ONE);
        GSOrderLine orderLine31 = new GSOrderLine(prod1, Units.of(5));
        GSOrderLine orderLine41 = new GSOrderLine(prod1, Units.TEN);
        order1.add(orderLine11); // füge GSOrderLine hinzu
        order1.add(orderLine12);
        order2.add(orderLine21);
        order3.add(orderLine31);
        order4.add(orderLine41);
//        System.out.println("orderManager.payOrder(order): " + orderManager.payOrder(order));
//        System.out.println("orderManager.completeOrder(order): " + orderManager.completeOrder(order).getStatus().toString());
//        System.out.println("order paid: " + order.isPaid());
//        System.out.println("order completed: " + order.isCompleted());
        orderManager.save(order1); // speichere die Order im OrderManager, damit dateCreated angelegt wird
        orderManager.save(order2); // speichere die Order im OrderManager, damit dateCreated angelegt wird
        orderManager.save(order3); // speichere die Order im OrderManager, damit dateCreated angelegt wird
        orderManager.save(order4); // speichere die Order im OrderManager, damit dateCreated angelegt wird
        orderRepo.save(order1);
        orderRepo.save(order2);
        orderRepo.save(order3);
        orderRepo.save(order4);

//        orderLine.increaseReclaimedAmount(BigDecimal.valueOf(5)); // reklamiere 5 Stück
//        orderManager.save(order);
//        orderRepo.save(order);
//        order.add(new GSOrderLine(prod, Units.ONE));
//        order.setOrderType(OrderType.RECLAIM);

        for (GSOrder o : orderRepo.findAll()) { // iteriere über alle gespeicherten Orders
            System.out.println("+++++ " + o + ": " + o.getOrderType() + " (isPaid() = " + o.isPaid() + ")");
            for (OrderLine ol : o.getOrderLines()) {
//                System.out.println("+++++ --- " + ((GSOrderLine) ol).getReclaimedAmount());
            }
        }
    }

    /**
     * Initializes {@link Message}s in {@link MessageRepository}.
     */
    private void initializeMessages() {
        if (messageRepo.count() > 0)
            return;

        GSOrder order = orderRepo.findByType(OrderType.RECLAIM).iterator().next();
        messageRepo.save(new Message(MessageKind.NOTIFICATION, "Testmessage"));
        messageRepo.save(new Message(MessageKind.RECLAIM, "Testreclaim (noch Weiterleitung auf catalog)", order));
    }
}
