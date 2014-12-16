package geekshop.controller;

import geekshop.model.*;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

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

    /**
     * Creates a new {@link CatalogController}.
     *
     * @param catalog       must not be {@literal null}.
     * @param supRepo       must not be {@literal null}.
     * @param subRepo       must not be {@literal null}.
     * @param userRepo      must not be {@literal null}.
     * @param passRulesRepo must not be {@literal null}.
     */


    @Autowired
    public CatalogController(Catalog<GSProduct> catalog, SuperCategoryRepository supRepo, SubCategoryRepository subRepo, UserRepository userRepo, PasswordRulesRepository passRulesRepo) {
        Assert.notNull(catalog, "Catalog must not be Null");
        Assert.notNull(supRepo, "SupRepo must not be Null");
        Assert.notNull(subRepo, "SubRepo must not be Null");
        Assert.notNull(userRepo, "UserRepo must not be Null");
        Assert.notNull(passRulesRepo, "PassRulesRepo must not be Null");

        this.catalog = catalog;
        this.supRepo = supRepo;
        this.subRepo = subRepo;
        this.userRepo = userRepo;
        this.passwordRules = passRulesRepo.findOne("passwordRules").get();
    }

    /**
     * shows the search Page with all {@Link Products} in the given {@Link SubCategory}
     */

    @RequestMapping("/productsearch/{subCategory}")
    public String catgory(Model model, @PathVariable("subCategory") String subCategory, @LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return AccountController.adjustPW(model, user, passwordRules);

        model.addAttribute("catalog", subRepo.findByName(subCategory).getProducts());
        model.addAttribute("superCategories", supRepo.findAll());
        model.addAttribute("subCategories", subRepo.findAll());
        return "productsearch";
    }

    /**
     * shows the search Page with all {@Link Products} which contain the searchTerm in their name
     */

    @RequestMapping("/productsearch")
    public String searchEntryByName(Model model, @RequestParam(value = "searchTerm", required = false) String searchTerm, @LoggedIn Optional<UserAccount> userAccount) {

        User user = userRepo.findByUserAccount(userAccount.get());

        if (user.pwHasToBeChanged())
            return AccountController.adjustPW(model, user, passwordRules);

        if (searchTerm == null) {
            model.addAttribute("catalog", catalog.findAll());
        } else
            model.addAttribute("catalog", search(searchTerm));
        model.addAttribute("superCategories", supRepo.findAll());
        model.addAttribute("subCategories", subRepo.findAll());
        return "productsearch";
    }

    private Set<GSProduct> search(String searchTerm) {
        Iterable<GSProduct> allProducts = catalog.findAll();
        Set<GSProduct> foundProducts = new HashSet<GSProduct>();
        for (GSProduct product : allProducts) {
            if (product.getName().toLowerCase().contains(searchTerm.toLowerCase()))
                foundProducts.add(product);
            if (product.productNumberToString(product.getProductNumber()).toLowerCase().contains(searchTerm.toLowerCase()))
                foundProducts.add(product);
            if (product.getSubCategory().getName().toLowerCase().contains(searchTerm.toLowerCase()))
                foundProducts.add(product);
            if (product.getSubCategory().getSuperCategory().getName().toLowerCase().contains(searchTerm.toLowerCase()))
                foundProducts.add(product);

        }
        return foundProducts;
    }

}
