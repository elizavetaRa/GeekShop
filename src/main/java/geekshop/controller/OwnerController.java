package geekshop.controller;

/*
 * Created by Basti on 20.11.2014.
 */

import geekshop.model.JokeRepository;
import geekshop.model.PasswordRules;
import geekshop.model.UserRepository;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * A Spring MVC controller to manage the shop owner's functions.
 *
 * @author Sebastian D&ouml;ring
 */

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

    @RequestMapping("/addemployee")
    public String addemployee(){
        return "addemployee";
    }

    @RequestMapping(value = "/addemployee", method = RequestMethod.POST)
    public String addemployee(@RequestParam("username") String username,
                              @RequestParam("firstname") String firstname,
                              @RequestParam("lastname") String lastname,
                              @RequestParam("gender") String gender,
                              @RequestParam("birthday") String birthday,
                              @RequestParam("maritalStatus") String maritalStatus,
                              @RequestParam("phone") String phone,
                              @RequestParam("street") String street,
                              @RequestParam("houseNr") String houseNr,
                              @RequestParam("postcode") String postcode,
                              @RequestParam("place") String place) {

        String password = new PasswordRules().generateRandomPassword();
//        UserAccount newUserAccount = UserAccountManager.create(username, password, new Role("ROLE_EMPLOYREE"));


        return "staff";
    }
}
