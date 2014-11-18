package GeekShop.web;

/**
 * Created by h4llow3En on 17/11/14.
 */

import GeekShop.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {


    @RequestMapping("/")
    public String index() {
        return "/index";
    }

    @RequestMapping(value = "/index", method = RequestMethod.POST)
    public String login(@RequestParam("userName") String userName, @RequestParam("password") String password){
        if (userName.isEmpty() || password.isEmpty()) {
            return "redirect:/index";
        } else{
            return "redirect:/main";
        }

    }
    @RequestMapping(value = "/main", method = RequestMethod.POST)
    public String home(){
        return "/main";
    }
}
