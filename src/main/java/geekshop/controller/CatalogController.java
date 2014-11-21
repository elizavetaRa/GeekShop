package geekshop.controller;

/**
 * Created by Basti on 20.11.2014.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CatalogController {

    @Autowired

    @RequestMapping("/catalog")
    public String catalog() {
        return "/catalog";
    }
}
