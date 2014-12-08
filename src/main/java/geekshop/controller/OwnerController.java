package geekshop.controller;

/*
 * Created by Basti on 20.11.2014.
 */

import geekshop.model.*;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountIdentifier;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * A Spring MVC controller to manage the shop owner's functions.
 *
 * @author Felix D&ouml;ring
 * @author Sebastian D&ouml;ring
 */

@Controller
@PreAuthorize("hasRole('ROLE_OWNER')")
class OwnerController {
    private final UserRepository userRepo;
    private final JokeRepository jokeRepo;
    private final UserAccountManager userAccountManager;
    private final MessageRepository messageRepo;

    @Autowired
    public OwnerController(UserRepository userRepo, JokeRepository jokeRepo, UserAccountManager userAccountManager, MessageRepository messageRepo) {
        this.userRepo = userRepo;
        this.jokeRepo = jokeRepo;
        this.userAccountManager = userAccountManager;
        this.messageRepo = messageRepo;
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

    @RequestMapping(value = "/jokes/{id}", method = RequestMethod.POST)
    public String showJoke(Model model, @PathVariable("id") Long id){
        Joke joke = jokeRepo.findJokeById(id);
        model.addAttribute("joke", joke);
        return "editjoke";
    }
    @RequestMapping(value = "/editjoke/{id}", method = RequestMethod.POST)
    public String editJoke(Model model, @PathVariable("id") Long id){
        return "redirect:/jokes";
    }

    @RequestMapping("/staff")
    public String staff(Model model) {

        List<User> employees = getEmployees();
        model.addAttribute("staff", employees);

        return "staff";
    }

    @RequestMapping("/messages")
    public String messages(Model model) {
        model.addAttribute("ownermessage", messageRepo.findAll());
        return "messages";
    }

    @RequestMapping("/addemployee")
    public String hire() {
        return "addemployee";
    }

    @RequestMapping(value = "/addemployee", method = RequestMethod.POST)
    public String hire(@RequestParam("username") String username,
                       @RequestParam("firstname") String firstname,
                       @RequestParam("mail") String mail,
                       @RequestParam("lastname") String lastname,
                       @RequestParam("gender") String strGender,
                       @RequestParam("birthday") String strBirthday,
                       @RequestParam("maritalStatus") String strMaritalStatus,
                       @RequestParam("phone") String phone,
                       @RequestParam("street") String street,
                       @RequestParam("houseNr") String houseNr,
                       @RequestParam("postcode") String postcode,
                       @RequestParam("place") String place) {

        String password = "test" /*new PasswordRules().generateRandomPassword()*/;
        UserAccount newUserAccount = userAccountManager.create(username, password, new Role("ROLE_EMPLOYREE"));
        newUserAccount.setFirstname(firstname);
        newUserAccount.setLastname(lastname);
        newUserAccount.setEmail(mail);
        userAccountManager.save(newUserAccount);
        Gender gender = strToGen(strGender);
        Date birthday = strToDate(strBirthday);
        if (birthday == null) return "/addemployee";
        MaritalStatus maritalStatus = strToMaritialStatus(strMaritalStatus);

        User newUser = new User(newUserAccount, gender, birthday, maritalStatus, phone, street, houseNr, postcode, place);
        userRepo.save(newUser);

        return "redirect:/staff";
    }

//    @RequestMapping("/staff/{username}")
//    public String showEmployee(Model model, @PathVariable("username") UserAccountIdentifier username) {
//        UserAccount userAccount = userAccountManager.get(username).get();
//        User user = userRepo.findByUserAccount(userAccount);
//        model.addAttribute("user", user);
//
//        return "profile";
//    }

    @RequestMapping(value = "/staff", method = RequestMethod.POST)
    public String showEmployee(Model model, @RequestParam("uai") UserAccountIdentifier uai) {
        UserAccount userAccount = userAccountManager.get(uai).get();
        User user = userRepo.findByUserAccount(userAccount);
        model.addAttribute("user", user);
        model.addAttribute("isOwnProfile", false);

        return "profile";
    }

    @RequestMapping(value = "/staff/{username}", method = RequestMethod.DELETE)
    public String fire(@PathVariable("username") UserAccountIdentifier username) {
        UserAccount userAccount = userAccountManager.get(username).get();
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

    public Date strToDate(String strDate) {
        strDate = strDate.replace(".", " ");
        strDate = strDate.replace("-", " ");
        strDate = strDate.replace("/", " ");
        Date date = null;
        try {
            date = new SimpleDateFormat("dd MM YYYY").parse(strDate);
        } catch (ParseException e) {

        }

        return date;

    }

    public MaritalStatus strToMaritialStatus(String strMaritalStatus) {
        MaritalStatus maritalStatus;
        if (strMaritalStatus.equals("UNMARRIED")) maritalStatus = MaritalStatus.UNMARRIED;
        if (strMaritalStatus.equals("MARRIED")) maritalStatus = MaritalStatus.MARRIED;
        if (strMaritalStatus.equals("SEPARATED")) maritalStatus = MaritalStatus.SEPARATED;
        if (strMaritalStatus.equals("DIVORCED")) maritalStatus = MaritalStatus.DIVORCED;
        if (strMaritalStatus.equals("WIDOWED")) maritalStatus = MaritalStatus.WIDOWED;
        if (strMaritalStatus.equals("PARTNERED")) maritalStatus = MaritalStatus.PARTNERED;
        if (strMaritalStatus.equals("NO_MORE_PARTNERED")) maritalStatus = MaritalStatus.NO_MORE_PARTNERED;
        if (strMaritalStatus.equals("PARTNER_LEFT_BEHIND")) maritalStatus = MaritalStatus.PARTNER_LEFT_BEHIND;
        else maritalStatus = MaritalStatus.UNKNOWN;

        return maritalStatus;
    }


    public Gender strToGen(String strGender) {
        Gender gender;
        if (strGender.equals("m")) gender = Gender.MALE;
        else if (strGender.equals("f")) gender = Gender.FEMALE;
        else gender = Gender.SOMETHING_ELSE;
        return gender;
    }

    public List<User> getEmployees(){
        Iterable<User> allUsers = userRepo.findAll();
        List<User> employees = new LinkedList<User>();
        for (User user : allUsers) {
            if (!user.getUserAccount().hasRole(new Role("ROLE_OWNER"))) {
                employees.add(user);
            }
        }
        return employees;
    }
}
