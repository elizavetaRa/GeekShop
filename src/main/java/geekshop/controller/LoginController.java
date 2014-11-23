package GeekShop.controller;

/**
 * Created by h4llow3En on 17/11/14.
 */

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {

    @RequestMapping({"/", "/index"})
    public String index() {
        return "/welcome";
    }


//    @RequestMapping(value = "/index", method = RequestMethod.POST)
//    public String login(@RequestParam("userName") String userName, @RequestParam("password") String password) {
//        if (userName.isEmpty() || password.isEmpty()) {
//            return "login";
//        } else {
//            return "welcome";
//        }
//    }

//    public String login(@RequestParam("userName") String userName, @RequestParam("password") String password, Model model){
//
//        if (userName.isEmpty() || password.isEmpty()) {
//            return "/index";
//        } else {
//            boolean found = false;
//            for (User user : userRepository.findAll()){
//                if (user.getUserName()== userName) {
//                    if (user.getPassword() == password) {
//                        found = true;
//                        break;
//                    }
//                }
//
//            }
//            if (found == false){
//                return "/index";
//            } else {
//                return "/main";
//            }
//        }
//   }


    @RequestMapping("/welcome")
    public String home() {
        return "welcome";
    }

    @RequestMapping("/profile")
    public String profile() {
        return "profile";
    }
}
