package geekshop.controller;

/**
 * Created by Basti on 20.11.2014.
 */

import geekshop.model.JokeRepository;
import geekshop.model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@PreAuthorize("hasRole('ROLE_OWNER')")
class OwnerController {
    private final UserRepository userRepo;
    private final JokeRepository jokeRepo;

    @Autowired
    public OwnerController(UserRepository userRepo, JokeRepository jokeRepo) {
        this.userRepo = userRepo;
        this.jokeRepo = jokeRepo;
    }

    @RequestMapping("/orders")
    public String orders() {
        return "orders";
    }

    @RequestMapping("/jokes")
    public String jokes(Model model) {
        model.addAttribute("jokes", jokeRepo.findAll());
        return "jokes";
    }

    @RequestMapping("/staff")
    public String staff() {
        return "staff";
    }

    @RequestMapping("/messages")
    public String messages() {
        return "messages";
    }
}
