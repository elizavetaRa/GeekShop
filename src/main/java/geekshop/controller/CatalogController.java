package geekshop.controller;

import geekshop.model.*;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final UserRepository userRepo;
    private final PasswordRules passwordRules;

    @Autowired
    public CatalogController(Catalog<GSProduct> catalog, SuperCategoryRepository supRepo, SubCategoryRepository subRepo, UserRepository userRepo, PasswordRulesRepository passRulesRepo) {
        this.catalog = catalog;
        this.supRepo = supRepo;
        this.subRepo = subRepo;
        this.userRepo = userRepo;
        this.passwordRules = passRulesRepo.findOne("passwordRules").get();
    }


    @RequestMapping("/productsearch")
    public String catalog(Model model, @LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return AccountController.adjustPW(model, user, passwordRules);

        model.addAttribute("superCategories", supRepo.findAll());
        model.addAttribute("subCategories", subRepo.findAll());
        model.addAttribute("catalog", catalog.findAll());
        return "productsearch";
    }

    @RequestMapping("/productsearch/{subCategory}")
    public String catgory(Model model, @PathVariable("subCategory") String subCategory, @LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return AccountController.adjustPW(model, user, passwordRules);

        model.addAttribute("specCategory", subRepo.findByName(subCategory));
        model.addAttribute("superCategories", supRepo.findAll());
        model.addAttribute("subCategories", subRepo.findAll());
        return "categorysearch";
    }

    @RequestMapping("/productsearch/name/{searchTerm}")
    public String searchEntryByName(Model model, @PathVariable("searchTerm") String searchTerm, @LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return AccountController.adjustPW(model, user, passwordRules);

//        List<GSProduct> list = searchForProducts(searchTerm);
        model.addAttribute("foundProducts", searchForProductName(searchTerm));
        model.addAttribute("superCategories", supRepo.findAll());
        model.addAttribute("subCategories", subRepo.findAll());
        return "extendedsearch";
    }


    @RequestMapping("/productsearch/id/{searchTerm}")
    public String searchEntryByID(Model model, @PathVariable("searchTerm") String searchTerm, @LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return AccountController.adjustPW(model, user, passwordRules);

        model.addAttribute("foundProducts", searchForProductID(searchTerm));
        model.addAttribute("superCategories", supRepo.findAll());
        model.addAttribute("subCategories", subRepo.findAll());
        return "extendedsearch";
    }


    @RequestMapping(value = "/extendedsearchname", method = RequestMethod.GET)
    public String searchProductByName(@RequestParam Map<String, String> formData) {
        String temp = formData.get("suchen");
        return "redirect:/productsearch/name/" + temp;
    }

    @RequestMapping(value = "/extendedsearchid", method = RequestMethod.GET)
    public String searchProductByID(@RequestParam Map<String, String> formData) {
        String temp = formData.get("suchenI");
        return "redirect:/productsearch/id/" + temp;
    }


    private List<GSProduct> searchForProductName(String searchTerm) {
        Iterable<GSProduct> allProducts = catalog.findAll();
        List<GSProduct> foundProducts = new LinkedList<GSProduct>();
        for (GSProduct product : allProducts) {
            if (product.getName().contains(searchTerm)) {
                foundProducts.add(product);
            }
        }
        return foundProducts;
    }

    private List<GSProduct> searchForProductID(String searchTerm) {
        Iterable<GSProduct> allProducts = catalog.findAll();
        List<GSProduct> foundProducts = new LinkedList<GSProduct>();
        for (GSProduct product : allProducts) {
            if (product.productNumberToString(product.getProductNumber()).contains(searchTerm)) {
                foundProducts.add(product);
            }
        }
        return foundProducts;
    }


}
