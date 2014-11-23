package GeekShop.controller;

/**
 * Created by Basti on 20.11.2014.
 */

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@PreAuthorize("isAuthenticated()")
public class CartController {

//    @Autowired

    @RequestMapping("/cart")
    public String cart() {
        return "/cart";
    }

    @RequestMapping("/reclaim")
    public String reclaim() {
        return "/reclaim";
    }
}
