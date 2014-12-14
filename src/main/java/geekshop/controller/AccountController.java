package geekshop.controller;

/*
 * Created by h4llow3En on 17/11/14.
 */

import geekshop.model.*;
import org.salespointframework.useraccount.*;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * A Spring MVC controller to manage the {@link User}s.
 *
 * @author Felix D&ouml;ring
 * @author Sebastian D&ouml;ring
 */

@Controller
@PreAuthorize("isAuthenticated()")
class AccountController {
    private final UserRepository userRepo;
    private final JokeRepository jokeRepo;
    private final PasswordRulesRepository passRulesRepo;
    private final PasswordRules passwordRules;
    private final UserAccountManager uam;
    private final AuthenticationManager authManager;
    private final MessageRepository messageRepo;

    /**
     * Creates a new {@link AccountController} with the given {@link UserRepository} and {@link JokeRepository}.
     *
     * @param userRepo      must not be {@literal null}.
     * @param jokeRepo      must not be {@literal null}.
     * @param passRulesRepo must not be {@literal null}.
     * @param uam           must not be {@literal null}.
     * @param authManager   must not be {@literal null}.
     * @param messageRepo   must not be {@literal null}.
     */
    @Autowired
    public AccountController(UserRepository userRepo, JokeRepository jokeRepo, PasswordRulesRepository passRulesRepo,
                             UserAccountManager uam, AuthenticationManager authManager, MessageRepository messageRepo) {
        Assert.notNull(userRepo, "UserRepository must not be null!");
        Assert.notNull(jokeRepo, "JokeRepository must not be null!");
        Assert.notNull(passRulesRepo, "PasswordRulesRepository must not be null!");
        Assert.isTrue(passRulesRepo.findAll().iterator().hasNext(), "PasswordRulesRepository should contain PasswordRules!");
        Assert.notNull(uam, "UserAccountManager must not be null!");
        Assert.notNull(authManager, "AuthenticationManager must not be null!");
        Assert.notNull(messageRepo, "MessageRepo must not be null!");

        this.userRepo = userRepo;
        this.jokeRepo = jokeRepo;
        this.passRulesRepo = passRulesRepo;
        this.passwordRules = passRulesRepo.findOne("passwordRules").get();
        this.uam = uam;
        this.authManager = authManager;
        this.messageRepo = messageRepo;
    }

    //region Login/Logout
    @RequestMapping({"/", "/index"})
    public String index(Model model, @LoggedIn Optional<UserAccount> userAccount, HttpSession httpSession) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return adjustPW(model, user, passwordRules);

        String sessionId = user.getCurrentSessionId();
        List<Joke> recentJokes = user.getRecentJokes();

        if (httpSession.getId().equals(sessionId)) {
            model.addAttribute("joke", user.getLastJoke());
        } else {
            Joke joke = getRandomJoke(recentJokes);

            user.addJoke(joke);
            user.setCurrentSessionId(httpSession.getId());
            userRepo.save(user);

            model.addAttribute("joke", joke);
        }
        return "welcome";
    }

    private Joke getRandomJoke(List<Joke> recentJokes) {
        List<Joke> allJokes = new LinkedList<Joke>();
        for (Joke j : jokeRepo.findAll()) {
            allJokes.add(j);
        }
        allJokes.removeAll(recentJokes);

        Joke joke;
        if (allJokes.isEmpty()) {
            joke = recentJokes.get(0);
        } else {
            int random = (new Random()).nextInt(allJokes.size());
            joke = allJokes.get(random);
        }
        return joke;
    }

    @RequestMapping("/logoff")
    public String logout(@LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (userAccount.get().hasRole(new Role("ROLE_OWNER")) && !passwordRules.isValidPassword(user.getPasswordAttributes())) {
            messageRepo.save(new Message(MessageKind.NOTIFICATION, "Passwort muss den geänderten Sicherheitsregeln entsprechend angepasst werden!"));
        }

        return "redirect:/logout";
    }

    public static String adjustPW(Model model, User user, PasswordRules passwordRules) {

        model.addAttribute("passwordRules", passwordRules);

        return "adjustpw";
    }

    @RequestMapping("/adjustpw")
    public String adjustPW() {
        return "redirect:/";
    }

    @RequestMapping(value = "/adjustpw", method = RequestMethod.POST)
    public String adjustPW(Model model, @RequestParam("newPW") String newPW, @RequestParam("retypePW") String retypePW, @LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        changePassword(model, user, newPW, retypePW);

        messageRepo.save(new Message(MessageKind.NOTIFICATION, user + " hat sein Passwort geändert."));

        return "redirect:/";
    }
    //endregion

    //region Staff (owner functions)
    @PreAuthorize("hasRole('ROLE_OWNER')")
    @RequestMapping("/staff")
    public String staff(Model model) {

        List<User> employees = getEmployees();
        model.addAttribute("staff", employees);

        return "staff";
    }

    @PreAuthorize("hasRole('ROLE_OWNER')")
    @RequestMapping("/staff/{uai}")
    public String showEmployee(Model model, @PathVariable("uai") UserAccountIdentifier uai) {
        UserAccount userAccount = uam.get(uai).get();
        User user = userRepo.findByUserAccount(userAccount);
        model.addAttribute("user", user);
        model.addAttribute("isOwnProfile", false);
        model.addAttribute("inEditingMode", false);

        return "profile";
    }

    @PreAuthorize("hasRole('ROLE_OWNER')")
    @RequestMapping("/addemployee")
    public String hire(Model model) {
        model.addAttribute("inEditingMode", true);
        return "profile";
    }

    @PreAuthorize("hasRole('ROLE_OWNER')")
    @RequestMapping(value = "/addemployee", method = RequestMethod.POST)
    public String hire(@RequestParam Map<String, String> formData) {

        String password = passwordRules.generateRandomPassword();
        UserAccount ua = uam.create(formData.get("username"), password, new Role("ROLE_EMPLOYEE"));
        ua.setFirstname(formData.get("firstname"));
        ua.setLastname(formData.get("lastname"));
        ua.setEmail(formData.get("email"));

        User user = new User(ua, password, Gender.valueOf(formData.get("gender")), OwnerController.strToDate(formData.get("birthday")),
                MaritalStatus.valueOf(formData.get("maritalStatus")), formData.get("phone"), formData.get("street"),
                formData.get("houseNr"), formData.get("postcode"), formData.get("place"));

        uam.save(ua);
        userRepo.save(user);

        String messageText = "Startpasswort des Nutzers " + formData.get("username") + ": " + password;
        messageRepo.save(new Message(MessageKind.NOTIFICATION, messageText));

        return "redirect:/staff";
    }

    @PreAuthorize("hasRole('ROLE_OWNER')")
    @RequestMapping(value = "/staff/{uai}", method = RequestMethod.DELETE)
    public String fire(@PathVariable("uai") UserAccountIdentifier uai) {
        UserAccount userAccount = uam.get(uai).get();
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

    @PreAuthorize("hasRole('ROLE_OWNER')")
    @RequestMapping("/staff/{uai}/{page}")
    public String profileChange(Model model, @PathVariable("uai") UserAccountIdentifier uai, @PathVariable("page") String page, @LoggedIn Optional<UserAccount> userAccount) {
        UserAccount ua = uam.get(uai).get();
        User user = userRepo.findByUserAccount(ua);
        model.addAttribute("user", user);

        if (page.equals("changedata")) {
            model.addAttribute("isOwnProfile", false);
            model.addAttribute("inEditingMode", true);

            return "profile";
        } else { // password change
            model.addAttribute("isOwnProfile", false);
            model.addAttribute("passwordRules", passwordRules);

            return "changepw";
        }
    }
    //endregion

    //region Setting PasswordRules (owner functions)
    @PreAuthorize("hasRole('ROLE_OWNER')")
    @RequestMapping("/setrules")
    public String setPWRules(Model model) {

        model.addAttribute("passwordRules", passwordRules);

        return "setrules";
    }

    @PreAuthorize("hasRole('ROLE_OWNER')")
    @RequestMapping(value = "/setrules", method = RequestMethod.POST)
    public String setPWRules(@RequestParam Map<String, String> map) {

        int minLength = Integer.parseInt(map.get("minLength"));
        boolean upperLower = map.get("upperLower") != null && Boolean.parseBoolean(map.get("upperLower"));
        boolean digits = map.get("digits") != null && Boolean.parseBoolean(map.get("digits"));
        boolean specialChars = map.get("specialChars") != null && Boolean.parseBoolean(map.get("specialChars"));

        if (minLength < 1)
            minLength = 1;

        passwordRules.setUpperAndLowerNecessary(upperLower);
        passwordRules.setDigitsNecessary(digits);
        passwordRules.setSpecialCharactersNecessary(specialChars);
        passwordRules.setMinLength(minLength);
        passRulesRepo.save(passwordRules);

        for (User user : userRepo.findAll()) {
            if (!user.getUserAccount().hasRole(new Role("ROLE_OWNER")) && !passwordRules.isValidPassword(user.getPasswordAttributes())) {
                user.setPwHasToBeChanged(true);
                userRepo.save(user);
            }
        }

        return "redirect:/staff";
    }
    //endregion

    //region Profile
    @RequestMapping("/profile")
    public String profile(Model model, @LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return adjustPW(model, user, passwordRules);

        model.addAttribute("user", user);

        model.addAttribute("isOwnProfile", true);
        model.addAttribute("inEditingMode", false);

        return "profile";
    }

    @RequestMapping("/profile/{page}")
    public String profileChange(Model model, @PathVariable("page") String page, @LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return adjustPW(model, user, passwordRules);

        if (page.equals("changedata")) {
            model.addAttribute("user", user);
            model.addAttribute("isOwnProfile", true);
            model.addAttribute("inEditingMode", true);

            return "profile";
        } else { // password change
            model.addAttribute("user", user);
            model.addAttribute("isOwnProfile", true);
            model.addAttribute("passwordRules", passwordRules);

            return "changepw";
        }
    }
    //endregion

    //region General account methods
    @RequestMapping(value = "/changeddata", method = RequestMethod.POST)
    public String changedData(Model model, @RequestParam Map<String, String> formData, @LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return adjustPW(model, user, passwordRules);

        String uai = formData.get("uai");
        UserAccount ua = uam.findByUsername(uai).get();
        user = userRepo.findByUserAccount(ua);

        ua.setFirstname(formData.get("firstname"));
        ua.setLastname(formData.get("lastname"));
        ua.setEmail(formData.get("email"));
        user.setGender(Gender.valueOf(formData.get("gender")));
        user.setBirthday(OwnerController.strToDate(formData.get("birthday")));
        user.setMaritalStatus(MaritalStatus.valueOf(formData.get("maritalStatus")));
        user.setPhone(formData.get("phone"));
        user.setStreet(formData.get("street"));
        user.setHouseNr(formData.get("houseNr"));
        user.setPostcode(formData.get("postcode"));
        user.setPlace(formData.get("place"));

        uam.save(ua);
        userRepo.save(user);

        if (userAccount.get().equals(ua)) {
            messageRepo.save(new Message(MessageKind.NOTIFICATION, user + " hat seine persönlichen Daten geändert."));
            return "redirect:/profile";
        } else
            return "redirect:/staff/" + uai;
    }

    @RequestMapping(value = "/changedownpw", method = RequestMethod.POST)
    public String changedOwnPW(Model model, @RequestParam("oldPW") String oldPW, @RequestParam("newPW") String newPW, @RequestParam("retypePW") String retypePW, @LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return adjustPW(model, user, passwordRules);

        if (oldPW.trim().isEmpty()) {
            System.out.println("Passwort ist leer!");
            model.addAttribute("error", "Passwort ist leer!");
        } else if (!authManager.matches(new Password(oldPW), userAccount.get().getPassword())) {
            System.out.println("Altes Passwort ist falsch!");
            model.addAttribute("error", "Altes Passwort ist falsch!");
        } else {
            changePassword(model, user, newPW, retypePW);
        }

        messageRepo.save(new Message(MessageKind.NOTIFICATION, user + " hat sein Passwort geändert."));

        return "redirect:/profile";
    }

    @RequestMapping(value = "/changedpw", method = RequestMethod.POST)
    public String changedPW(Model model, @RequestParam("newPW") String newPW, @RequestParam("retypePW") String retypePW, @RequestParam("uai") UserAccountIdentifier uai, @LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return adjustPW(model, user, passwordRules);

        UserAccount ua = uam.get(uai).get();
        user = userRepo.findByUserAccount(ua);

        changePassword(model, user, newPW, retypePW);

        return "redirect:/staff/" + uai.toString();
    }

    private boolean changePassword(Model model, User user, String newPW, String retypePW) {

        if (newPW.trim().isEmpty()) {
            System.out.println("Passwort ist leer!");
            model.addAttribute("error", "Passwort ist leer!");

            return false;
        }

        if (!newPW.equals(retypePW)) {
            System.out.println("Passwörter stimmen nicht überein!");
            model.addAttribute("error", "Passwörter stimmen nicht überein!");

            return false;
        }

        if (!passwordRules.isValidPassword(newPW)) {
            System.out.println("Neues Passwort entspricht nicht den Sicherheitsregeln!");
            model.addAttribute("error", "Neues Passwort entspricht nicht den Sicherheitsregeln!");

            return false;
        }

        uam.changePassword(user.getUserAccount(), newPW);

        PasswordAttributes pwAttributes = user.getPasswordAttributes();
        pwAttributes.setHasUpperAndLower(PasswordRules.containsUpperAndLower(newPW));
        pwAttributes.setHasDigits(PasswordRules.containsDigits(newPW));
        pwAttributes.setHasSpecialCharacters(PasswordRules.containsSpecialCharacters(newPW));
        pwAttributes.setLength(newPW.length());
        user.setPwHasToBeChanged(false);
        userRepo.save(user);

        return true;
    }

    private List<User> getEmployees() {
        Iterable<User> allUsers = userRepo.findAll();
        List<User> employees = new LinkedList<User>();
        for (User user : allUsers) {
            if (!user.getUserAccount().hasRole(new Role("ROLE_OWNER"))) {
                employees.add(user);
            }
        }
        return employees;
    }
    //endregion
}
