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
    private final PasswordRules passwordRules;
    private final UserAccountManager uam;
    private final AuthenticationManager authManager;

    /**
     * Creates a new {@link AccountController} with the given {@link UserRepository} and {@link JokeRepository}.
     *
     * @param userRepo      must not be {@literal null}.
     * @param jokeRepo      must not be {@literal null}.
     * @param passRulesRepo must not be {@literal null}.
     * @param uam           must not be {@literal null}.
     * @param authManager   must not be {@literal null}.
     */
    @Autowired
    public AccountController(UserRepository userRepo,
                             JokeRepository jokeRepo,
                             PasswordRulesRepository passRulesRepo,
                             UserAccountManager uam,
                             AuthenticationManager authManager) {
        Assert.notNull(userRepo, "UserRepository must not be null!");
        Assert.notNull(jokeRepo, "JokeRepository must not be null!");
        Assert.notNull(passRulesRepo, "PasswordRulesRepository must not be null!");
        Assert.isTrue(passRulesRepo.findAll().iterator().hasNext(), "PasswordRulesRepository should contain PasswordRules!");
        Assert.notNull(uam, "UserAccountManager must not be null!");
        Assert.notNull(authManager, "AuthenticationManager must not be null!");

        this.userRepo = userRepo;
        this.jokeRepo = jokeRepo;
        this.passwordRules = passRulesRepo.findOne("passwordRules").get();
        this.uam = uam;
        this.authManager = authManager;
    }

    @RequestMapping({"/", "/index"})
    public String index(Model model, @LoggedIn Optional<UserAccount> userAccount, HttpSession httpSession) {
        List<Joke> recentJokes;
        User user = null;
        String sessionId = null;
        if (userAccount.isPresent()) {
            user = userRepo.findByUserAccount(userAccount.get());
            sessionId = user.getCurrentSessionId();
            recentJokes = user.getRecentJokes();
        } else {
            recentJokes = new LinkedList<Joke>();
        }

        if (user != null && httpSession.getId().equals(sessionId)) {
            model.addAttribute("joke", user.getLastJoke());
        } else {
            Joke joke = getRandomJoke(recentJokes);
            if (user != null) {
                user.addJoke(joke);
                user.setCurrentSessionId(httpSession.getId());
                userRepo.save(user);
            }
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

//    @RequestMapping("/welcome")
//    public String home() {
//        return "welcome";
//    }

    @RequestMapping("/profile")
    public String profile(Model model, @LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.isPresent()) {
            User user = userRepo.findByUserAccount(userAccount.get());
            model.addAttribute("user", user);
        }
        model.addAttribute("isOwnProfile", true);

        return "profile";
    }

    @RequestMapping(value = "/profile", method = RequestMethod.POST)
    public String changeData(@RequestParam Map<String, String> formData, @LoggedIn Optional<UserAccount> userAccount) {
        String uai = formData.get("uai");
        UserAccount ua = uam.get(new UserAccountIdentifier(uai)).get();
        User user = userRepo.findByUserAccount(ua);

        ua.setFirstname(formData.get("firstname"));
        ua.setLastname(formData.get("lastname"));
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

        if (userAccount.get().equals(ua))
            return "redirect:/profile";
        else
            return "redirect:/staff/" + uai.toString();
    }

    @RequestMapping(value = "/changeOwnPW", method = RequestMethod.POST)
    public String changeOwnPassword(@RequestParam("oldPW") String oldPW, @RequestParam("newPW") String newPW, @RequestParam("retypePW") String retypePW, @LoggedIn Optional<UserAccount> userAccount) {
        if (!userAccount.isPresent())
            throw new IllegalArgumentException("There should be a user logged in.");

        User user = userRepo.findByUserAccount(userAccount.get());
        if (oldPW.trim().isEmpty() || newPW.trim().isEmpty()) {
            System.out.println("Passwort ist leer!");
        } else if (!authManager.matches(new Password(oldPW), userAccount.get().getPassword())) {
            System.out.println("Altes Passwort ist falsch!");
        } else if (!newPW.equals(retypePW)) {
            System.out.println("Passwörter stimmen nicht überein!");
        } else if (!passwordRules.isValidPassword(newPW)) {
            System.out.println("Neues Passwort entspricht nicht den Sicherheitsregeln!");
        } else {
            uam.changePassword(user.getUserAccount(), newPW);
        }

        return "redirect:/profile";
    }

    @RequestMapping(value = "/changePW", method = RequestMethod.POST)
    public String changePassword(@RequestParam("newPW") String newPW, @RequestParam("retypePW") String retypePW, @RequestParam("uai") UserAccountIdentifier uai) {

        UserAccount ua = uam.get(uai).get();
        User user = userRepo.findByUserAccount(ua);
        if (newPW.trim().isEmpty()) {
            System.out.println("Passwort ist leer!");
        } else if (!newPW.equals(retypePW)) {
            System.out.println("Passwörter stimmen nicht überein!");
        } else if (!passwordRules.isValidPassword(newPW)) {
            System.out.println("Neues Passwort entspricht nicht den Sicherheitsregeln!");
        } else {
            uam.changePassword(user.getUserAccount(), newPW);
        }

        return "redirect:/staff/" + uai.toString();
    }
}
