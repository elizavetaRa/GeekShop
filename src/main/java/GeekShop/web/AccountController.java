package GeekShop.web;

/**
 * Created by h4llow3En on 17/11/14.
 */

import GeekShop.*;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AccountController {

    private final UserRepository userRepository;

    @Autowired
    public AccountController(UserRepository userRepository) {
        Assert.notNull(userRepository, "Has not to be Null.");
        this.userRepository = userRepository;
    }


    @RequestMapping({"/", "/index"})
    public String index() {
        return "/index";
    }



    @RequestMapping(value = "/index", method = RequestMethod.POST)
    public String login(@RequestParam("userName") String userName, @RequestParam("password") String password, Model model){

        if (userName.isEmpty() || password.isEmpty()) {
            return "/index";
        } else {
            boolean found = false;
            for (User user : userRepository.findAll()){
                if (user.getUserName()== userName) {
                    if (user.getPassword() == password) {
                        found = true;
                        break;
                    }
                }

            }
            if (found == false){
                return "/index";
            } else {
                return "/main";
            }
        }

    }
    @RequestMapping(value = "/main")
    public String home(){
        return "/main";
    }

    @RequestMapping(value = "/profile")
    public String profile(){
        return "/profile";
    }
}
