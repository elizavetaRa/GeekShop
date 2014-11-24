package GeekShop.controller;

/**
 * Created by Basti on 20.11.2014.
 */

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@PreAuthorize("hasRole('ROLE_OWNER')")
class OwnerController {

//    @Autowired

    @RequestMapping("/jokes")
    public String jokes() {
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
