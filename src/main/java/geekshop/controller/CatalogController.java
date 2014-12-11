package geekshop.controller;

/*
 * Created by Basti on 20.11.2014.
 */

import geekshop.model.GSProduct;
import geekshop.model.SubCategoryRepository;
import geekshop.model.SuperCategoryRepository;
import org.salespointframework.catalog.Catalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    private final SuperCategoryRepository supRepo;
    private final SubCategoryRepository subRepo;

    @Autowired
    public CatalogController(Catalog<GSProduct> catalog, SuperCategoryRepository supRepo, SubCategoryRepository subRepo) {
        this.catalog = catalog;
        this.supRepo = supRepo;
        this.subRepo = subRepo;
    }


    @RequestMapping("/productsearch")
    public String catalog(Model model) {
        model.addAttribute("superCategories", supRepo.findAll());
        model.addAttribute("subCategories", subRepo.findAll());
        model.addAttribute("catalog", catalog.findAll());
        return "productsearch";
    }

}
