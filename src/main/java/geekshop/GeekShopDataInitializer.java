package GeekShop;

import GeekShop.model.Joke;
import GeekShop.model.JokeRepository;
import GeekShop.model.User;
import GeekShop.model.UserRepository;
import org.salespointframework.core.DataInitializer;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountIdentifier;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Arrays;

/**
 * Created by Basti on 23.11.2014.
 */

@Component
public class GeekShopDataInitializer implements DataInitializer {

    //    private final Inventory<InventoryItem> inventory;
//    private final VideoCatalog videoCatalog;
    private final UserAccountManager userAccountManager;
    private final UserRepository userRepo;
    private final JokeRepository jokeRepo;

    @Autowired
    public GeekShopDataInitializer(UserRepository userRepo, JokeRepository jokeRepo/*, Inventory<InventoryItem> inventory*/,
                                   UserAccountManager userAccountManager/*, VideoCatalog videoCatalog*/) {

        Assert.notNull(userRepo, "UserRepository must not be null!");
        Assert.notNull(jokeRepo, "JokeRepository must not be null!");
//        Assert.notNull(inventory, "Inventory must not be null!");
        Assert.notNull(userAccountManager, "UserAccountManager must not be null!");
//        Assert.notNull(videoCatalog, "VideoCatalog must not be null!");

        this.userRepo = userRepo;
        this.jokeRepo = jokeRepo;
//        this.inventory = inventory;
        this.userAccountManager = userAccountManager;
//        this.videoCatalog = videoCatalog;
    }

    /*
     * (non-Javadoc)
     * @see org.salespointframework.core.DataInitializer#initialize()
     */
    @Override
    public void initialize() {

        initializeUsers(userAccountManager, userRepo);
        initializeJokes(jokeRepo);
//        initializeCatalog(videoCatalog, inventory);
    }

//    private void initializeCatalog(VideoCatalog videoCatalog, Inventory<InventoryItem> inventory) {
//
//        if (videoCatalog.findAll().iterator().hasNext()) {
//            return;
//        }
//
//        videoCatalog.save(new Disc("Last Action Hero", "lac", Money.of(EUR, 9.99), "Äktschn/Comedy", DiscType.DVD));
//        videoCatalog.save(new Disc("Back to the Future", "bttf", Money.of(EUR, 9.99), "Sci-Fi", DiscType.DVD));
//        videoCatalog.save(new Disc("Fido", "fido", Money.of(EUR, 9.99), "Comedy/Drama/Horror", DiscType.DVD));
//        videoCatalog.save(new Disc("Super Fuzz", "sf", Money.of(EUR, 9.99), "Action/Sci-Fi/Comedy", DiscType.DVD));
//        videoCatalog.save(new Disc("Armour of God II: Operation Condor", "aog2oc", Money.of(EUR, 14.99),
//                "Action/Adventure/Comedy", DiscType.DVD));
//        videoCatalog.save(new Disc("Persepolis", "pers", Money.of(EUR, 14.99), "Animation/Biography/Drama", DiscType.DVD));
//        videoCatalog
//                .save(new Disc("Hot Shots! Part Deux", "hspd", Money.of(EUR, 9999.0), "Action/Comedy/War", DiscType.DVD));
//        videoCatalog.save(new Disc("Avatar: The Last Airbender", "tla", Money.of(EUR, 19.99), "Animation/Action/Adventure",
//                DiscType.DVD));
//
//        videoCatalog.save(new Disc("Secretary", "secretary", Money.of(EUR, 6.99), "Political Drama", DiscType.BLURAY));
//        videoCatalog.save(new Disc("The Godfather", "tg", Money.of(EUR, 19.99), "Crime/Drama", DiscType.BLURAY));
//        videoCatalog.save(new Disc("No Retreat, No Surrender", "nrns", Money.of(EUR, 29.99), "Martial Arts",
//                DiscType.BLURAY));
//        videoCatalog.save(new Disc("The Princess Bride", "tpb", Money.of(EUR, 39.99), "Adventure/Comedy/Family",
//                DiscType.BLURAY));
//        videoCatalog.save(new Disc("Top Secret!", "ts", Money.of(EUR, 39.99), "Comedy", DiscType.BLURAY));
//        videoCatalog.save(new Disc("The Iron Giant", "tig", Money.of(EUR, 34.99), "Animation/Action/Adventure",
//                DiscType.BLURAY));
//        videoCatalog.save(new Disc("Battle Royale", "br", Money.of(EUR, 19.99), "Action/Drama/Thriller", DiscType.BLURAY));
//        videoCatalog.save(new Disc("Oldboy", "old", Money.of(EUR, 24.99), "Action/Drama/Thriller", DiscType.BLURAY));
//        videoCatalog.save(new Disc("Bill & Ted's Excellent Adventure", "bt", Money.of(EUR, 29.99),
//                "Adventure/Comedy/Family", DiscType.BLURAY));
//
//        // (｡◕‿◕｡)
//        // Über alle eben hinzugefügten Discs iterieren und jeweils ein InventoryItem mit der Quantity 10 setzen
//        // Das heißt: Von jeder Disc sind 10 Stück im Inventar.
//
//        for (Disc disc : videoCatalog.findAll()) {
//            InventoryItem inventoryItem = new InventoryItem(disc, Units.TEN);
//            inventory.save(inventoryItem);
//        }
//    }

    private void initializeUsers(UserAccountManager userAccountManager, UserRepository userRepo) {

        // (｡◕‿◕｡)
        // UserAccounts bestehen aus einem Identifier und eine Password, diese werden auch für ein Login gebraucht
        // Zusätzlich kann ein UserAccount noch Rollen bekommen, diese können in den Controllern und im View dazu genutzt
        // werden
        // um bestimmte Bereiche nicht zugänglich zu machen, das "ROLE_"-Prefix ist eine Konvention welche für Spring
        // Security nötig ist.

        // Skip creation if database was already populated
        if (userAccountManager.get(new UserAccountIdentifier("owner")).isPresent()) {
            return;
        }

        UserAccount ownerAccount = userAccountManager.create("owner", "123", new Role("ROLE_OWNER"));
        userAccountManager.save(ownerAccount);

        final Role employeeRole = new Role("ROLE_EMPLOYEE");

        UserAccount ua1 = userAccountManager.create("hans", "123", employeeRole);
        userAccountManager.save(ua1);
        UserAccount ua2 = userAccountManager.create("dextermorgan", "123", employeeRole);
        userAccountManager.save(ua2);
        UserAccount ua3 = userAccountManager.create("earlhickey", "123", employeeRole);
        userAccountManager.save(ua3);
        UserAccount ua4 = userAccountManager.create("mclovinfogell", "123", employeeRole);
        userAccountManager.save(ua4);

        User owner = new User(ownerAccount);
        User u1 = new User(ua1);
        User u2 = new User(ua2);
        User u3 = new User(ua3);
        User u4 = new User(ua4);

        userRepo.save(Arrays.asList(owner, u1, u2, u3, u4));
    }

    private void initializeJokes(JokeRepository jokeRepo) {
        if (jokeRepo.count() > 0) {
            return;
        }

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
                "und brüllt durch ein offenes Fenster: „Wo sind wir?“" + System.getProperty("line.separator") + "Ein Mann blickt von seinem PC auf: " +
                "„In einem Flugzeug!“\nDer Pilot dreht eine scharfe Kurve und " +
                "landet fünf Minuten später mit dem letzten Tropfen Treibstoff auf dem Flughafen von Seattle.\n" +
                "Die verblüfften Passagiere wollen wissen, wie der Pilot es geschafft habe, sich zu orientieren.\n" +
                "„Ganz einfach“, sagt der Pilot. „Die Antwort auf meine Frage war kurz, korrekt und völlig nutzlos. " +
                "Ich hatte also mit der Microsoft-Hotline gesprochen. " +
                "Das Microsoft Gebäude liegt 5 Meilen westlich vom Flughafen Seattle, Kurs 89 Grad.“"
        ));
        jokeRepo.save(new Joke(
                "Am Straßenrand steht ein Auto mit einem platten Reifen. Woran erkennt man, dass der Fahrer Informatiker ist?" + System.getProperty("line.separator") +
                "– Wenn er nachsieht, ob an den anderen Reifen der gleiche Fehler auftritt."
        ));
    }

}
