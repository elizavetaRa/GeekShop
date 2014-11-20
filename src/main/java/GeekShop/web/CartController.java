package GeekShop.web;

/**
 * Created by Basti on 20.11.2014.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CartController {

    @Autowired

    @RequestMapping("/cart")
    public String cart() {
        return "/cart";
    }

    @RequestMapping("/reclaim")
    public String reclaim() {
        return "/reclaim";
    }
}
