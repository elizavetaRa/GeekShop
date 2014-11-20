package GeekShop.web;

/**
 * Created by Basti on 20.11.2014.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class OwnerController {

    @Autowired

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
