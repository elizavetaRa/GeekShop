package GeekShop.web;

/**
 * Created by h4llow3En on 17/11/14.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AccountController {


    @Autowired

    @RequestMapping({"/", "/index"})
    public String index() {
        return "/login";
    }


    @RequestMapping(value = "/index", method = RequestMethod.POST)
    public String login(@RequestParam("userName") String userName, @RequestParam("password") String password) {
        if (userName.isEmpty() || password.isEmpty()) {
            return "login";
        } else {
            return "welcome";
        }

    }


    @RequestMapping("/welcome")
    public String home() {
        return "welcome";
    }

    @RequestMapping("/profile")
    public String profile() {
        return "profile";
    }
}
