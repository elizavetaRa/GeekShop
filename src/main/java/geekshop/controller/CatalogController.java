package geekshop.controller;

/*
 * Created by Basti on 20.11.2014.
 */

import geekshop.model.GSProduct;
import geekshop.model.SubCategory;
import geekshop.model.SubCategoryRepository;
import geekshop.model.SuperCategoryRepository;
import org.salespointframework.catalog.Catalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * A Spring MVC controller to manage the {@link org.salespointframework.catalog.Catalog}.
 *
 * @author Sebastian D&ouml;ring
 * @author Marcus Kammerdiener
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

    @RequestMapping("/productsearch/{subCategory}")
    public String profile(Model model, @PathVariable("subCategory") String subCategory) {
            model.addAttribute("specCategory", subRepo.findByName(subCategory));
            model.addAttribute("superCategories", supRepo.findAll());
            model.addAttribute("subCategories", subRepo.findAll());
            model.addAttribute("catalog", catalog.findAll());
            model.addAttribute("count", subRepo.findByName(subCategory).getProducts().size());
        return "categorysearch";
    }


}
