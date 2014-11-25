package geekshop.controller;

/**
 * Created by h4llow3En on 17/11/14.
 */

import geekshop.model.Joke;
import geekshop.model.JokeRepository;
import geekshop.model.User;
import geekshop.model.UserRepository;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Controller
@PreAuthorize("isAuthenticated()")
class AccountController {
    private final UserRepository userRepo;
    private final JokeRepository jokeRepo;

    @Autowired
    public AccountController(UserRepository userRepo, JokeRepository jokeRepo) {
        this.userRepo = userRepo;
        this.jokeRepo = jokeRepo;
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
    public String profile() {
        return "profile";
    }
}
