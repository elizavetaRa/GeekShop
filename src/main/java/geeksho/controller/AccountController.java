package geeksho.controller;

/**
 * Created by h4llow3En on 17/11/14.
 */

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AccountController {

//    private final UserRepository userRepository;

//    @Autowired
//    public AccountController(UserRepository userRepository) {
//        Assert.notNull(userRepository, "Has not to be Null.");
//        this.userRepository = userRepository;
//    }

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
