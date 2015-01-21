package geekshop.controller;

import geekshop.model.*;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

/**
 * A Spring MVC controller to manage the {@link org.salespointframework.catalog.Catalog}.
 *
 * @author Marcus Kammerdiener
 * @author Sebastian Döring
 */

@Controller
@PreAuthorize("isAuthenticated()")
class CatalogController {

    private final Catalog<GSProduct> catalog;
    private final SuperCategoryRepository supRepo;
    private final SubCategoryRepository subRepo;
    private final UserRepository userRepo;
    private final Inventory<GSInventoryItem> inventory;

    /**
     * Creates a new {@link CatalogController}.
     *
     * @param catalog  must not be {@literal null}.
     * @param supRepo  must not be {@literal null}.
     * @param subRepo  must not be {@literal null}.
     * @param userRepo must not be {@literal null}.
     */


    @Autowired
    public CatalogController(Catalog<GSProduct> catalog, SuperCategoryRepository supRepo, SubCategoryRepository subRepo, UserRepository userRepo, Inventory<GSInventoryItem> inventory) {
        Assert.notNull(catalog, "Catalog must not be Null");
        Assert.notNull(supRepo, "SupRepo must not be Null");
        Assert.notNull(subRepo, "SubRepo must not be Null");
        Assert.notNull(userRepo, "UserRepo must not be Null");
        Assert.notNull(inventory, "Inventory must not be Null");

        this.catalog = catalog;
        this.supRepo = supRepo;
        this.subRepo = subRepo;
        this.userRepo = userRepo;
        this.inventory = inventory;
    }


    /**
     * Shows the search page with all {@link org.salespointframework.catalog.Product}s which contain the search term ({@code q}) in their name,
     * or if search term is not set, shows all {@link org.salespointframework.catalog.Product}s belonging to the chosen category ({@code cat}).
     * The list of results is sorted by the given sorting method ({@code sort}).
     */

    @RequestMapping("/productsearch")
    public String searchEntryByName(Model model, @LoggedIn Optional<UserAccount> userAccount,
                                    @RequestParam(value = "q", required = false) String searchTerm,
                                    @RequestParam(value = "sort", required = false) String sorting,
                                    @RequestParam(value = "cat", required = false) String category) {
        if (userAccount.get().hasRole(new Role("ROLE_INSECURE_PASSWORD")))
            return "redirect:/";

        if (searchTerm == null || searchTerm.isEmpty()) {
            if (category == null || category.isEmpty()) {
                if (sorting == null || sorting.isEmpty() || sorting.equals("name")) {
                    model.addAttribute("catalog", sortProductByName(findAllProducts()));
                } else if (sorting.equals("prodnum")) {
                    model.addAttribute("catalog", sortProductByProductNumber(findAllProducts()));
                } else if (sorting.equals("pricedesc")) {
                    model.addAttribute("catalog", sortProductByPrice(findAllProducts(), "desc"));
                } else if (sorting.equals("priceasc")) {
                    model.addAttribute("catalog", sortProductByPrice(findAllProducts(), "asc"));
                }
            } else {
                Iterable<GSProduct> catalog;
                if (supRepo.findByName(category) == null) {
                    if (subRepo.findByName(category) == null) {
                        catalog = findAllProducts();
                    } else {
                        catalog = subRepo.findByName(category).getProducts();
                    }
                } else {
                    catalog = getAllProductsInSuperCategory(supRepo.findByName(category));
                }
                if (sorting == null || sorting.isEmpty() || sorting.equals("name")) {
                    model.addAttribute("catalog", sortProductByName(catalog));
                } else if (sorting.equals("prodnum")) {
                    model.addAttribute("catalog", sortProductByProductNumber(catalog));
                } else if (sorting.equals("pricedesc")) {
                    model.addAttribute("catalog", sortProductByPrice(catalog, "desc"));
                } else if (sorting.equals("priceasc")) {
                    model.addAttribute("catalog", sortProductByPrice(catalog, "asc"));
                }
            }
        } else if (sorting == null || sorting.isEmpty() || sorting.equals("name")) {
            model.addAttribute("catalog", sortProductByName(search(searchTerm)));
        } else if (sorting.equals("prodnum")) {
            model.addAttribute("catalog", sortProductByProductNumber(search(searchTerm)));
        } else if (sorting.equals("pricedesc")) {
            model.addAttribute("catalog", sortProductByPrice(search(searchTerm), "desc"));
        } else if (sorting.equals("priceasc")) {
            model.addAttribute("catalog", sortProductByPrice(search(searchTerm), "asc"));
        }
        model.addAttribute("superCategories", supRepo.findAll());
        model.addAttribute("subCategories", sortSubCategoryByName(subRepo.findAll()));
        model.addAttribute("inventory", inventory);
        return "productsearch";
    }

    /**
     * Searches the {@link Catalog} for {@link GSProduct}s which contain the given {@literal searchTerm}, are in a {@link SubCategory} containing the
     * given {@literal searchTerm} or are in a {@link SuperCategory} containing the given {@literal searchTerm}.
     * @return a Set with the {@link GSProduct}s matching the criteria.
     */

    private Set<GSProduct> search(String searchTerm) {
        Iterable<GSProduct> allProducts = findAllProducts();
        Set<GSProduct> foundProducts = new HashSet<GSProduct>();
        for (GSProduct product : allProducts) {
            if (searchTerm.matches("\\d+")) {
                if (product.getProductNumber() == Long.parseLong(searchTerm))
                    foundProducts.add(product);
            } else {
                if (product.getName().toLowerCase().contains(searchTerm.toLowerCase())
                        || product.getSubCategory().getName().toLowerCase().contains(searchTerm.toLowerCase())
                        || product.getSubCategory().getSuperCategory().getName().toLowerCase().contains(searchTerm.toLowerCase()))
                    foundProducts.add(product);
            }
        }
        return foundProducts;
    }

    /**
     * Sorts an Iterable of {@link GSProduct}s by name.
     * @return a sorted List of {@link GSProduct}s.
     */

    private List<GSProduct> sortProductByName(Iterable<GSProduct> foundProducts) {
        List<GSProduct> sortedProducts = new LinkedList<>();
        for (GSProduct product : foundProducts) {
            sortedProducts.add(product);
        }
        Collections.sort(sortedProducts, (GSProduct a, GSProduct b) -> (a.getName().compareTo(b.getName())));
        return sortedProducts;
    }

    /**
     * Sorts an Iterable of {@link GSProduct}s by ProductNumber.
     * @return a sorted List of {@link GSProduct}s.
     */

    private List<GSProduct> sortProductByProductNumber(Iterable<GSProduct> foundProducts) {
        List<GSProduct> sortedProducts = new LinkedList<>();
        for (GSProduct product : foundProducts) {
            sortedProducts.add(product);
        }
        Collections.sort(sortedProducts, (GSProduct a, GSProduct b) -> (Long.compare(a.getProductNumber(), b.getProductNumber())));
        return sortedProducts;
    }

    /**
     * Sorts an Iterable of {@link GSProduct}s by Price.
     * @return a sorted List of {@link GSProduct}s.
     */

    private List<GSProduct> sortProductByPrice(Iterable<GSProduct> foundProducts, String direction) {
        List<GSProduct> sortedProducts = new LinkedList<>();
        for (GSProduct product : foundProducts) {
            sortedProducts.add(product);
        }
        if (direction == "asc")
            Collections.sort(sortedProducts, (GSProduct a, GSProduct b) -> (Double.compare(a.getPrice().getAmount().doubleValue(), b.getPrice().getAmount().doubleValue())));
        else
            Collections.sort(sortedProducts, (GSProduct a, GSProduct b) -> (Double.compare(b.getPrice().getAmount().doubleValue(), a.getPrice().getAmount().doubleValue())));
        return sortedProducts;
    }

    /**
     * Sorts the Iterable of {@link SubCategory} for a persistent display in the front-end.
     * @return a List of subcategories.
     */

    private List<SubCategory> sortSubCategoryByName(Iterable<SubCategory> foundCategories) {
        List<SubCategory> sortedSubCategory = new LinkedList<>();
        for (SubCategory subCategory : foundCategories) {
            sortedSubCategory.add(subCategory);
        }
        Collections.sort(sortedSubCategory, (SubCategory a, SubCategory b) -> (a.getName().compareTo(b.getName())));

        return sortedSubCategory;
    }

    /**
     * Finds all {@link GSProduct}s in the {@link Catalog} that are in range.
     *
     * @return an Iterable with all {@link GSProduct}s in the {@link Catalog} that are in range.
     */

    private Iterable<GSProduct> findAllProducts() {
        Collection<GSProduct> products = new HashSet<>();
        for (GSProduct p : catalog.findAll()) {
            if (p.isInRange())
                products.add(p);
        }
        return products;
    }

    /**
     * Determines all {@link GSProduct}s in the given {@link SuperCategory}.
     *
     * @return a Set of all {@link GSProduct}s in the given {@link SuperCategory}.
     */
    private Set<GSProduct> getAllProductsInSuperCategory(SuperCategory superCategory) {
        List<SubCategory> temp = superCategory.getSubCategories();
        Set<GSProduct> products = new HashSet<GSProduct>();
        for (SubCategory subCategory : temp) {
            products.addAll(subCategory.getProducts());
        }
        return products;
    }
}
