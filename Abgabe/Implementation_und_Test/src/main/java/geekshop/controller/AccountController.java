package geekshop.controller;

import geekshop.model.*;
import geekshop.model.validation.PersonalDataForm;
import geekshop.model.validation.SetRulesForm;
import org.salespointframework.useraccount.*;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * A Spring MVC controller to manage functionalities concerning shop owner's and employee's account.
 *
 * @author Sebastian Döring
 * @author Felix Döring
 */

@Controller
@PreAuthorize("isAuthenticated()")
class AccountController {
    private final UserRepository userRepo;
    private final JokeRepository jokeRepo;
    private final PasswordRulesRepository passRulesRepo;
    private final UserAccountManager uam;
    private final AuthenticationManager authManager;
    private final MessageRepository messageRepo;


    @Autowired
    public AccountController(UserRepository userRepo, JokeRepository jokeRepo, PasswordRulesRepository passRulesRepo,
                             UserAccountManager uam, AuthenticationManager authManager, MessageRepository messageRepo) {
        Assert.notNull(userRepo, "UserRepository must not be null!");
        Assert.notNull(jokeRepo, "JokeRepository must not be null!");
        Assert.notNull(passRulesRepo, "PasswordRulesRepository must not be null!");
        Assert.notNull(uam, "UserAccountManager must not be null!");
        Assert.notNull(authManager, "AuthenticationManager must not be null!");
        Assert.notNull(messageRepo, "MessageRepo must not be null!");

        this.userRepo = userRepo;
        this.jokeRepo = jokeRepo;
        this.passRulesRepo = passRulesRepo;
        this.uam = uam;
        this.authManager = authManager;
        this.messageRepo = messageRepo;
    }

    //region Login/Logout

    /**
     * Shows the start page with the welome joke or, if the user's password does not match the current password rules,
     * redirects to the respective page demanding the user to change the password according to the current password rules.
     */
    @RequestMapping({"/", "/index"})
    public String index(Model model, @LoggedIn Optional<UserAccount> userAccount, HttpSession session) {

        User user = userRepo.findByUserAccount(userAccount.get());

        // add user for full name in header to the session
        session.setAttribute("user", user);

        // add message repository to the session to display current number of messages
        session.setAttribute("msgRepo", messageRepo);

        // add flag to the session marking whether we are in reclaiming process or normal process
        session.setAttribute("isReclaim", true);

        //add flag for correct orderoverview
        session.setAttribute("overview", true);

        // check whether user's password matches the current password rules
        PasswordRules passwordRules = passRulesRepo.findOne("passwordRules").get();

        if (!passwordRules.isValidPassword(user.getPasswordAttributes())) {
            if (userAccount.get().hasRole(new Role("ROLE_OWNER"))) {
                if (!messageRepo.findByMessageKind(MessageKind.PASSWORD).iterator().hasNext())
                    messageRepo.save(new Message(MessageKind.PASSWORD, "Passwort muss den geänderten Sicherheitsregeln entsprechend angepasst werden!"));
            } else {
                userAccount.get().add(new Role("ROLE_INSECURE_PASSWORD"));
                userRepo.save(user);
                model.addAttribute("passwordRules", passwordRules);
                return "adjustpw";
            }
        } else if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD"))) {
            userAccount.get().remove(new Role("ROLE_INSECURE_PASSWORD"));
            userRepo.save(user);
        }

        // arrange new welcome joke
        List<Joke> recentJokes = user.getRecentJokes();

        if (session.getAttribute("jokeDisplayed") != null && (boolean) session.getAttribute("jokeDisplayed")) {
            model.addAttribute("joke", user.getLastJoke());
        } else {
            Joke joke = getRandomJoke(recentJokes);

            if (joke != null)
                user.addJoke(joke);
            userRepo.save(user);

            session.setAttribute("jokeDisplayed", true);

            model.addAttribute("joke", joke);
        }
        return "welcome";
    }

    /**
     * Determines a random {@link Joke} out of the {@link JokeRepository} which is not contained in the list of recent jokes.
     */
    public Joke getRandomJoke(List<Joke> recentJokes) {
        List<Joke> allJokes = new LinkedList<Joke>();
        for (Joke j : jokeRepo.findAll()) {
            allJokes.add(j);
        }
        allJokes.removeAll(recentJokes);

        Joke joke;
        if (allJokes.isEmpty()) {
            if (recentJokes.isEmpty())
                return null;
            joke = recentJokes.get(0);
        } else {
            int random = (new Random()).nextInt(allJokes.size());
            joke = allJokes.get(random);
        }
        return joke;
    }

    /**
     * Redirects to start page if {@code /adjustpw} is mistakenly requested directly.
     */
    @RequestMapping("/adjustpw")
    public String adjustPW() {
        return "redirect:/";
    }

    /**
     * Saves the changed password of a user demanded after login to change his password which did not match the password rules.
     */
    @RequestMapping(value = "/adjustpw", method = RequestMethod.POST)
    public String adjustPW(Model model, @RequestParam("newPW") String newPW, @RequestParam("retypePW") String retypePW, @LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (!validateNewPW(model, newPW, retypePW)) {
            model.addAttribute("newPW", newPW);
            model.addAttribute("retypePW", retypePW);
            model.addAttribute("passwordRules", passRulesRepo.findOne("passwordRules").get());
            return "adjustpw";
        } else {
            changePassword(user, newPW);
            messageRepo.save(new Message(MessageKind.NOTIFICATION, user + " hat sein Passwort geändert."));

            return "redirect:/";
        }
    }
    //endregion

    //region Staff (owner functions)

    /**
     * Shows the overview of all employees.
     */
    @PreAuthorize("hasRole('ROLE_OWNER')")
    @RequestMapping("/staff")
    public String staff(Model model) {

        List<User> employees = getEmployees();
        model.addAttribute("staff", employees);

        return "staff";
    }

    /**
     * Shows an employee's profile.
     */
    @PreAuthorize("hasRole('ROLE_OWNER')")
    @RequestMapping("/staff/{uai}")
    public String showEmployee(Model model, @PathVariable("uai") UserAccountIdentifier uai) {
        UserAccount userAccount = uam.get(uai).get();
        User user = userRepo.findByUserAccount(userAccount);
        model.addAttribute("personalDataForm",
                new PersonalDataForm(
                        user.getUserAccount().getFirstname(), user.getUserAccount().getLastname(),
                        user.getUserAccount().getUsername(), user.getUserAccount().getEmail(),
                        user.getGender(), user.dateOfBirthToString(), user.getMaritalStatus(), user.getPhone(),
                        user.getStreet(), user.getHouseNr(), user.getPostcode(), user.getPlace())
        );
        model.addAttribute("isOwnProfile", false);
        model.addAttribute("inEditingMode", false);

        return "profile";
    }

    /**
     * Shows the form for adding a new employee.
     */
    @PreAuthorize("hasRole('ROLE_OWNER')")
    @RequestMapping("/addemployee")
    public String hire(Model model) {
        model.addAttribute("inEditingMode", true);
        model.addAttribute("personalDataForm", new PersonalDataForm());
        return "profile";
    }

    /**
     * Saves the new employee's data. The shop owner gets a message containing the initial password of the new user.
     */
    @PreAuthorize("hasRole('ROLE_OWNER')")
    @RequestMapping(value = "/addemployee", method = RequestMethod.POST)
    public String hire(Model model, @ModelAttribute("personalDataForm") @Valid PersonalDataForm personalDataForm,
                       BindingResult result) {

        if (personalDataForm.getUsername() != null && !personalDataForm.getUsername().trim().isEmpty() && uam.findByUsername(personalDataForm.getUsername()).isPresent()) {
            result.addError(new FieldError("personalDataForm", "username", "Benutzername existiert bereits."));
        }
        if (result.hasErrors()) {
            model.addAttribute("inEditingMode", true);
            return "profile";
        }

        PasswordRules passwordRules = passRulesRepo.findOne("passwordRules").get();
        String password = passwordRules.generateRandomPassword();
        // create insecure password to force the new employee to change this initial password
        password = password.substring(0, passwordRules.getMinLength() - 1);
        UserAccount ua = uam.create(personalDataForm.getUsername(), password, new Role("ROLE_EMPLOYEE"));
        ua.setFirstname(personalDataForm.getFirstname().trim());
        ua.setLastname(personalDataForm.getLastname().trim());
        ua.setEmail(personalDataForm.getEmail().trim());

        User user = new User(ua, password, personalDataForm.getGender(), User.strToDate(personalDataForm.getDateOfBirth().trim()),
                personalDataForm.getMaritalStatus(), personalDataForm.getPhone().trim(), personalDataForm.getStreet().trim(),
                personalDataForm.getHouseNr().trim(), personalDataForm.getPostcode().trim(), personalDataForm.getPlace().trim());

        uam.save(ua);
        userRepo.save(user);

        String messageText = "Startpasswort des neuen Angestellten " + user + ": " + password;
        messageRepo.save(new Message(MessageKind.NOTIFICATION, messageText));

        return "redirect:/staff/" + urlEncode(ua.getId().toString());
    }

    /**
     * Dismisses an employee.
     */
    @PreAuthorize("hasRole('ROLE_OWNER')")
    @RequestMapping(value = "/staff/{uai}", method = RequestMethod.DELETE)
    public String fire(@PathVariable("uai") UserAccountIdentifier uai) {
        UserAccount userAccount = uam.get(uai).get();
        if (userAccount.hasRole(new Role("ROLE_OWNER"))) {
            return "redirect:/staff";
        } else {
            dismiss(userRepo.findByUserAccount(userAccount));
            return "redirect:/staff";
        }
    }

    /**
     * Dismisses all employees.
     */
    @PreAuthorize("hasRole('ROLE_OWNER')")
    @RequestMapping(value = "/firestaff", method = RequestMethod.DELETE)
    public String fireAll() {
        Iterable<User> allEmployees = userRepo.findAll();
        for (User user : allEmployees) {
            if (user.getUserAccount().hasRole(new Role("ROLE_OWNER")) && user.getUserAccount().isEnabled())
                continue;
            dismiss(user);
        }

        return "redirect:/staff";
    }

    /**
     * Depending on {@code page}, it shows the form either for data modification or for password change of an employee.
     */
    @PreAuthorize("hasRole('ROLE_OWNER')")
    @RequestMapping("/staff/{uai}/{page}")
    public String profileChange(Model model, @PathVariable("uai") UserAccountIdentifier uai, @PathVariable("page") String page) {

        UserAccount ua = uam.get(uai).get();
        User user = userRepo.findByUserAccount(ua);

        switch (page) {
            case "changedata":
                model.addAttribute("personalDataForm",
                        new PersonalDataForm(
                                user.getUserAccount().getFirstname(), user.getUserAccount().getLastname(),
                                user.getUserAccount().getUsername(), user.getUserAccount().getEmail(),
                                user.getGender(), user.dateOfBirthToString(), user.getMaritalStatus(), user.getPhone(),
                                user.getStreet(), user.getHouseNr(), user.getPostcode(), user.getPlace())
                );
                model.addAttribute("isOwnProfile", false);
                model.addAttribute("inEditingMode", true);

                return "profile";

            case "changepw":
                model.addAttribute("fullname", user.toString());
                model.addAttribute("isOwnProfile", false);
                model.addAttribute("passwordRules", passRulesRepo.findOne("passwordRules").get());

                return "changepw";

            default:
                return "redirect:/staff/" + urlEncode(uai.toString());
        }
    }

    /**
     * Saves the new user data changed by the shop owner.
     */
    @PreAuthorize("hasRole('ROLE_OWNER')")
    @RequestMapping(value = "/staff/{uai}/changedata", method = RequestMethod.POST)
    public String changedData(Model model, @PathVariable("uai") UserAccountIdentifier uai,
                              @ModelAttribute("personalDataForm") @Valid PersonalDataForm personalDataForm, BindingResult result) {

        UserAccount ua = uam.get(uai).get();
        User user = userRepo.findByUserAccount(ua);

        if (result.hasErrors()) {
            model.addAttribute("personalDataForm", personalDataForm);
            model.addAttribute("isOwnProfile", true);
            model.addAttribute("inEditingMode", true);

            return "profile";
        }

        changeData(user, personalDataForm);

        uam.save(ua);
        userRepo.save(user);

        return "redirect:/staff/" + urlEncode(uai.toString());
    }

    /**
     * Saves the new password of an employee changed by the shop owner.
     */
    @PreAuthorize("hasRole('ROLE_OWNER')")
    @RequestMapping(value = "staff/{uai}/changepw", method = RequestMethod.POST)
    public String changedPW(Model model, @PathVariable("uai") UserAccountIdentifier uai, @RequestParam("newPW") String newPW, @RequestParam("retypePW") String retypePW) {

        UserAccount ua = uam.get(uai).get();
        User user = userRepo.findByUserAccount(ua);

        if (!validateNewPW(model, newPW, retypePW)) {
            model.addAttribute("fullname", user.toString());
            model.addAttribute("newPW", newPW);
            model.addAttribute("retypePW", retypePW);
            model.addAttribute("isOwnProfile", false);
            model.addAttribute("passwordRules", passRulesRepo.findOne("passwordRules").get());
            return "changepw";
        } else {
            changePassword(user, newPW);
            messageRepo.save(new Message(MessageKind.NOTIFICATION, "Neues Passwort von Nutzer " + user + ": " + newPW));
            return "redirect:/staff/" + urlEncode(uai.toString());
        }
    }
    //endregion

    //region Setting PasswordRules (owner functions)

    /**
     * Shows the form for changing the password safety rules.
     */
    @PreAuthorize("hasRole('ROLE_OWNER')")
    @RequestMapping("/setrules")
    public String setPWRules(Model model) {

        model.addAttribute("passwordRules", passRulesRepo.findOne("passwordRules").get());
        model.addAttribute("setRulesForm", new SetRulesForm());

        return "setrules";
    }

    /**
     * Saves the changed password rules.
     */
    @PreAuthorize("hasRole('ROLE_OWNER')")
    @RequestMapping(value = "/setrules", method = RequestMethod.POST)
    public String setPWRules(Model model, @RequestParam Map<String, String> map,
                             @ModelAttribute("setRulesForm") @Valid SetRulesForm setRulesForm, BindingResult result) {

        if (result.hasErrors()) {
            model.addAttribute("passwordRules", passRulesRepo.findOne("passwordRules").get());
            return "setrules";
        }

        int minLength = Integer.parseInt(map.get("minLength"));
        if (minLength < 6)
            minLength = 6;

        boolean upperLower = map.get("upperLower") != null && Boolean.parseBoolean(map.get("upperLower"));
        boolean digits = map.get("digits") != null && Boolean.parseBoolean(map.get("digits"));
        boolean specialChars = map.get("specialChars") != null && Boolean.parseBoolean(map.get("specialChars"));

        PasswordRules passwordRules = passRulesRepo.findOne("passwordRules").get();
        passwordRules.setUpperAndLowerNecessary(upperLower);
        passwordRules.setDigitsNecessary(digits);
        passwordRules.setSpecialCharactersNecessary(specialChars);
        passwordRules.setMinLength(minLength);
        passRulesRepo.save(passwordRules);

        return "redirect:/staff";
    }
    //endregion

    //region Profile

    /**
     * Shows the user's profile page.
     */
    @RequestMapping("/profile")
    public String profile(Model model, @LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        User user = userRepo.findByUserAccount(userAccount.get());

        model.addAttribute("personalDataForm",
                new PersonalDataForm(
                        user.getUserAccount().getFirstname(), user.getUserAccount().getLastname(),
                        user.getUserAccount().getUsername(), user.getUserAccount().getEmail(),
                        user.getGender(), user.dateOfBirthToString(), user.getMaritalStatus(), user.getPhone(),
                        user.getStreet(), user.getHouseNr(), user.getPostcode(), user.getPlace())
        );
        model.addAttribute("isOwnProfile", true);
        model.addAttribute("inEditingMode", false);

        return "profile";
    }

    /**
     * Depending on {@code page}, it shows the form either for data modification or for password change.
     */
    @RequestMapping("/profile/{page}")
    public String profileChange(Model model, @PathVariable("page") String page, @LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        User user = userRepo.findByUserAccount(userAccount.get());

        switch (page) {
            case "changedata":
                model.addAttribute("personalDataForm",
                        new PersonalDataForm(
                                user.getUserAccount().getFirstname(), user.getUserAccount().getLastname(),
                                user.getUserAccount().getUsername(), user.getUserAccount().getEmail(),
                                user.getGender(), user.dateOfBirthToString(), user.getMaritalStatus(), user.getPhone(),
                                user.getStreet(), user.getHouseNr(), user.getPostcode(), user.getPlace())
                );
                model.addAttribute("isOwnProfile", true);
                model.addAttribute("inEditingMode", true);

                return "profile";

            case "changepw":
                model.addAttribute("fullname", user.toString());
                model.addAttribute("isOwnProfile", true);
                model.addAttribute("passwordRules", passRulesRepo.findOne("passwordRules").get());

                return "changepw";

            default:
                return "redirect:/profile";
        }
    }

    /**
     * Saves the new user data changed by the user himself. If the user is an employee, a message will be sent to the shop owner.
     */
    @RequestMapping(value = "/profile/changedata", method = RequestMethod.POST)
    public String changedOwnData(Model model, HttpSession session, @LoggedIn Optional<UserAccount> userAccount,
                                 @ModelAttribute("personalDataForm") @Valid PersonalDataForm personalDataForm, BindingResult result) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        UserAccount ua = userAccount.get();
        User user = userRepo.findByUserAccount(ua);

        if (result.hasErrors()) {
            model.addAttribute("personalDataForm", personalDataForm);
            model.addAttribute("isOwnProfile", true);
            model.addAttribute("inEditingMode", true);

            return "profile";
        }

        changeData(user, personalDataForm);

        uam.save(ua);
        userRepo.save(user);

        if (!userAccount.get().hasRole(new Role("ROLE_OWNER")))
            messageRepo.save(new Message(MessageKind.NOTIFICATION, user + " hat seine persönlichen Daten geändert."));

        session.setAttribute("user", user);

        return "redirect:/profile";
    }

    /**
     * Saves the new password. If the password has been changed by an employee, a message will be sent to the shop owner.
     */
    @RequestMapping(value = "/profile/changepw", method = RequestMethod.POST)
    public String changedOwnPW(Model model, @RequestParam("oldPW") String oldPW, @RequestParam("newPW") String newPW, @RequestParam("retypePW") String retypePW, @LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        User user = userRepo.findByUserAccount(userAccount.get());

        boolean hasErrors = false;

        if (oldPW.trim().isEmpty()) {
            model.addAttribute("oldPWError", "Altes Passwort ist leer.");
            hasErrors = true;
        } else if (!authManager.matches(new Password(oldPW), userAccount.get().getPassword())) {
            model.addAttribute("oldPWError", "Altes Passwort ist falsch.");
            hasErrors = true;
        }

        if (!validateNewPW(model, newPW, retypePW)) {
            hasErrors = true;
        }

        if (hasErrors) {
            model.addAttribute("fullname", user.toString());
            model.addAttribute("oldPW", oldPW);
            model.addAttribute("newPW", newPW);
            model.addAttribute("retypePW", retypePW);
            model.addAttribute("isOwnProfile", true);
            model.addAttribute("passwordRules", passRulesRepo.findOne("passwordRules").get());
            return "changepw";
        } else {
            changePassword(user, newPW);
            if (userAccount.get().hasRole(new Role("ROLE_OWNER"))) {
                messageRepo.delete(messageRepo.findByMessageKind(MessageKind.PASSWORD));
            } else {
                messageRepo.save(new Message(MessageKind.NOTIFICATION, user + " hat sein Passwort geändert."));
            }
            return "redirect:/profile";

        }
    }
    //endregion

    //region General account methods

    /**
     * Does the real work by changing the user's personal data with the given {@link PersonalDataForm}.
     */
    private void changeData(User user, PersonalDataForm pdf) {
        user.getUserAccount().setFirstname(pdf.getFirstname().trim());
        user.getUserAccount().setLastname(pdf.getLastname().trim());
        user.getUserAccount().setEmail(pdf.getEmail().trim());
        user.setGender(pdf.getGender());
        user.setDateOfBirth(User.strToDate(pdf.getDateOfBirth().trim()));
        user.setMaritalStatus(pdf.getMaritalStatus());
        user.setPhone(pdf.getPhone().trim());
        user.setStreet(pdf.getStreet().trim());
        user.setHouseNr(pdf.getHouseNr().trim());
        user.setPostcode(pdf.getPostcode().trim());
        user.setPlace(pdf.getPlace().trim());
    }

    /**
     * Validates whether the given new password is not empty, is valid according to the current password rules
     * and matches the given retyped password.
     * @return {@literal true} if the new password is valid
     */
    private boolean validateNewPW(Model model, String newPW, String retypePW) {
        boolean hasErrors = false;

        if (newPW.trim().isEmpty()) {
            model.addAttribute("newPWError", "Neues Passwort ist leer.");
            hasErrors = true;
        } else if (!passRulesRepo.findOne("passwordRules").get().isValidPassword(newPW)) {
            model.addAttribute("newPWError", "Neues Passwort entspricht nicht den Sicherheitsregeln.");
            hasErrors = true;
        }

        if (!newPW.trim().isEmpty()) {
            if (retypePW.trim().isEmpty()) {
                model.addAttribute("retypePWError", "Geben Sie das neue Passwort nochmals ein.");
                hasErrors = true;
            } else if (!newPW.equals(retypePW)) {
                model.addAttribute("retypePWError", "Passwörter stimmen nicht überein.");
                hasErrors = true;
            }
        }

        return !hasErrors;
    }

    /**
     * Does the real work by changing the user's password and updating his {@link PasswordAttributes}.
     */
    private void changePassword(User user, String newPW) {

        uam.changePassword(user.getUserAccount(), newPW);

        PasswordAttributes pwAttributes = user.getPasswordAttributes();
        pwAttributes.setHasUpperAndLower(PasswordRules.containsUpperAndLower(newPW));
        pwAttributes.setHasDigits(PasswordRules.containsDigits(newPW));
        pwAttributes.setHasSpecialCharacters(PasswordRules.containsSpecialCharacters(newPW));
        pwAttributes.setLength(newPW.length());
        userRepo.save(user);
    }

    /**
     * Does the real dismissal work by removing the employee role and disabling the user account.
     * However, the {@link User} cannot be removed from {@link UserRepository} because the user could be still present in current orders.
     */
    private void dismiss(User user) {
        UserAccount userAccount = user.getUserAccount();
        userAccount.remove(new Role("ROLE_EMPLOYEE"));
        uam.disable(userAccount.getIdentifier());
        uam.save(userAccount);
    }

    /**
     * Creates a list of all employees needed for staff overview.
     */
    private List<User> getEmployees() {
        Iterable<User> allUsers = userRepo.findAll();
        List<User> employees = new LinkedList<User>();
        for (User user : allUsers) {
            if (!user.getUserAccount().hasRole(new Role("ROLE_OWNER")) && user.getUserAccount().isEnabled()) {
                employees.add(user);
            }
        }
        return employees;
    }

    /**
     * Helper method to url-encode a string.
     *
     * @param str String to be encoded
     */
    private String urlEncode(String str) {
        try {
            str = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            str = "";
        }
        return str;
    }
    //endregion
}
