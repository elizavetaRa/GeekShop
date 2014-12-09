package geekshop.controller;

/*
 * Created by Basti on 20.11.2014.
 */

import geekshop.model.*;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.order.OrderManager;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * A Spring MVC controller to manage the {@link org.salespointframework.catalog.Catalog}.
 *
 * @author Sebastian D&ouml;ring
 */

@Controller
@PreAuthorize("isAuthenticated()")
class CatalogController {

    private final Catalog<GSProduct> catalog;

    @Autowired
    public CatalogController(Catalog<GSProduct> catalog) {
        this.catalog = catalog;
    }

    @RequestMapping("/catalog")
    public String catalog(Model model) {
        model.addAttribute("catalog", catalog.findAll());
        return "/catalog";
    }
}
