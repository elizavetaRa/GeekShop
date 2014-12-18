package geekshop.controller;

import geekshop.model.*;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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

    /**
     * Creates a new {@link CatalogController}.
     *
     * @param catalog  must not be {@literal null}.
     * @param supRepo  must not be {@literal null}.
     * @param subRepo  must not be {@literal null}.
     * @param userRepo must not be {@literal null}.
     */


    @Autowired
    public CatalogController(Catalog<GSProduct> catalog, SuperCategoryRepository supRepo, SubCategoryRepository subRepo, UserRepository userRepo) {
        Assert.notNull(catalog, "Catalog must not be Null");
        Assert.notNull(supRepo, "SupRepo must not be Null");
        Assert.notNull(subRepo, "SubRepo must not be Null");
        Assert.notNull(userRepo, "UserRepo must not be Null");

        this.catalog = catalog;
        this.supRepo = supRepo;
        this.subRepo = subRepo;
        this.userRepo = userRepo;
    }

    /**
     * shows the search Page with all {@Link Products} in the given {@Link SubCategory}
     */

    @RequestMapping("/productsearch/{subCategory}")
    public String catgory(Model model, @PathVariable("subCategory") String subCategory, @LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        model.addAttribute("catalog", subRepo.findByName(subCategory).getProducts());
        model.addAttribute("superCategories", supRepo.findAll());
        model.addAttribute("subCategories", subRepo.findAll());
        return "productsearch";
    }

     /**
     * shows the search Page with all {@Link Products} which contain the searchTerm in their name
     */

    @RequestMapping("/productsearch")
    public String searchEntryByName(Model model, @RequestParam(value = "searchTerm", required = false) String searchTerm, @RequestParam(value = "sorting", required = false) String sorting, @LoggedIn Optional<UserAccount> userAccount) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        if (searchTerm == null) {
            if ((sorting == null) || (sorting.matches("Name"))) {
                model.addAttribute("catalog", sortProductByName(catalog.findAll()));
            }
            else if (sorting.matches("Artikelnummer")) {
                model.addAttribute("catalog", sortProductByProductNumber(catalog.findAll()));
            }
            else if (sorting.matches("Preis absteigend")){
                model.addAttribute("catalog", sortProductByPrice(catalog.findAll(), "desc"));
            }
            else if (sorting.matches("Preis aufsteigend")) {
                model.addAttribute("catalog", sortProductByPrice(catalog.findAll(), "asc"));
            }
        }
        else if ((sorting == null) || (sorting.matches("Name")))
            model.addAttribute("catalog", sortProductByName(search(searchTerm)));
        else if (sorting.matches("Artikelnummer"))
            model.addAttribute("catalog", sortProductByProductNumber(search(searchTerm)));
        else if (sorting.matches("Preis absteigend"))
            model.addAttribute("catalog", sortProductByPrice(search(searchTerm), "desc"));
        else if (sorting.matches("Preis aufsteigend"))
            model.addAttribute("catalog", sortProductByPrice(search(searchTerm), "asc"));
        model.addAttribute("superCategories", supRepo.findAll());
        model.addAttribute("subCategories", sortSubCategoryByName(subRepo.findAll()));
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

    private List<GSProduct> sortProductByName(Iterable<GSProduct> foundProducts) {
        List<GSProduct> sortedProducts = new LinkedList<>();
        for (GSProduct product : foundProducts) {
            sortedProducts.add(product);
        }
        Collections.sort(sortedProducts, (GSProduct a, GSProduct b) -> (a.getName().compareTo(b.getName())));
        return sortedProducts;
    }


    private List<GSProduct> sortProductByProductNumber(Iterable<GSProduct> foundProducts) {
        List<GSProduct> sortedProducts = new LinkedList<>();
        for (GSProduct product : foundProducts) {
            sortedProducts.add(product);
        }
        Collections.sort(sortedProducts, (GSProduct a, GSProduct b) -> (Integer.compare(a.getProductNumber(), b.getProductNumber())));
        return sortedProducts;
    }

    private List<GSProduct> sortProductByPrice(Iterable<GSProduct> foundProducts, String direction) {
        List<GSProduct> sortedProducts = new LinkedList<>();
        for (GSProduct product : foundProducts) {
            sortedProducts.add(product);
        }
        if (direction == "asc")
        Collections.sort(sortedProducts, (GSProduct a, GSProduct b) -> (Double.compare(a.getPriceDouble(), b.getPriceDouble())));
        else Collections.sort(sortedProducts, (GSProduct a, GSProduct b) -> (Double.compare(b.getPriceDouble(), a.getPriceDouble())));
        return sortedProducts;
    }


    private List<SubCategory> sortSubCategoryByName(Iterable<SubCategory> foundCategories) {
        List<SubCategory> sortedSubCategory = new LinkedList<>();
        for (SubCategory subCategory : foundCategories) {
            sortedSubCategory.add(subCategory);
        }
        Collections.sort(sortedSubCategory, (SubCategory a, SubCategory b) -> (a.getName().compareTo(b.getName())));

        return sortedSubCategory;
    }
}
