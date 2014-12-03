package geekshop.controller;

/*
 * Created by Basti on 20.11.2014.
 */

import com.sun.tools.javac.jvm.Gen;
import geekshop.model.*;
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

import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    private final UserAccountManager userAccountManager;

    @Autowired
    public OwnerController(UserRepository userRepo, JokeRepository jokeRepo, UserAccountManager userAccountManager) {
        this.userRepo = userRepo;
        this.jokeRepo = jokeRepo;
        this.userAccountManager = userAccountManager;
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
        userAccountManager.save(newUserAccount);
        Gender gender = strToGen(strGender);
        Date birthday = strToDate(strBirthday);
        if (birthday == null) return "wrongdate";
        MaritalStatus maritalStatus = strToMaritialStatus(strMaritalStatus);

        User newUser = new User(newUserAccount, firstname, lastname, gender, birthday, maritalStatus, phone, street, houseNr, postcode, place);
        userRepo.save(newUser);

        return "staff";
    }

    public Date strToDate(String strDate){
        strDate = strDate.replace(".", " ");
        strDate = strDate.replace("-", " ");
        strDate = strDate.replace("/", " ");
        Date date = null;
        try {
            date = new SimpleDateFormat("dd MM YYYY").parse(strDate);
        } catch (ParseException e){

        }

        return date;

    }

    public MaritalStatus strToMaritialStatus(String strMaritalStatus) {
        MaritalStatus maritalStatus;
        if (strMaritalStatus == "UNMARRIED") maritalStatus = MaritalStatus.UNMARRIED;
        if (strMaritalStatus == "MARRIED") maritalStatus = MaritalStatus.MARRIED;
        if (strMaritalStatus == "SEPARATED") maritalStatus = MaritalStatus.SEPARATED;
        if (strMaritalStatus == "DIVORCED") maritalStatus = MaritalStatus.DIVORCED;
        if (strMaritalStatus == "WIDOWED") maritalStatus = MaritalStatus.WIDOWED;
        if (strMaritalStatus == "PARTNERED") maritalStatus = MaritalStatus.PARTNERED;
        if (strMaritalStatus == "NO_MORE_PARTNERED") maritalStatus = MaritalStatus.NO_MORE_PARTNERED;
        if (strMaritalStatus == "PARTNER_LEFT_BEHIND") maritalStatus = MaritalStatus.PARTNER_LEFT_BEHIND;
        else maritalStatus = MaritalStatus.UNKNOWN;

        return maritalStatus;
    }


    public Gender strToGen(String strGender){
        Gender gender;
        if (strGender == "m") gender = Gender.MALE;
        else if(strGender == "f") gender = Gender.FEMALE;
        else gender = Gender.SOMETHING_ELSE;
        return gender;
    }
}
