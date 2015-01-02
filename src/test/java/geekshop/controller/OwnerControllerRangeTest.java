package geekshop.controller;

import geekshop.AbstractWebIntegrationTests;
import geekshop.model.*;
import org.junit.Test;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.ProductIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterChainProxy;

import static org.junit.Assert.assertTrue;

public class OwnerControllerRangeTest extends AbstractWebIntegrationTests {

    @Autowired
    OwnerController controller;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    FilterChainProxy securityFilterChain;

    @Autowired
    SubCategoryRepository subCategoryRepo;
    @Autowired
    SuperCategoryRepository superCategoryRepo;
    @Autowired
    Catalog<GSProduct> catalog;


    protected void login(String username, String password) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
        SecurityContextHolder.getContext().setAuthentication(authenticationManager.authenticate(authentication));
    }

    @Test
    public void addSuperCat() throws Exception {
        login("owner", "123");
        controller.addSuperCategory("Test");


        assertTrue(testSuper("Test"));

    }

    @Test
    public void editSuper() throws Exception {

        login("owner", "123");
        SuperCategory superCategory = superCategoryRepo.findAll().iterator().next();
        String superCat = superCategory.getName();
        Long id = superCategory.getId();

        controller.editSuper("newName", superCat);

        assertTrue(superCategoryRepo.findOne(id).get().getName() == "newName");
    }


    public Boolean testSuper(String name) {
        if (superCategoryRepo.findByName(name) == null) {
            return false;
        } else {
            return true;
        }
    }


    @Test
    public void addSubCat() throws Exception {
        login("owner", "123");
        String superCategory = superCategoryRepo.findAll().iterator().next().getName();
        controller.addSubCategory("Test", superCategory);


        assertTrue(testSub("Test"));

    }

    @Test
    public void editSub() throws Exception {

        login("owner", "123");
        SubCategory subCategory = subCategoryRepo.findAll().iterator().next();
        String superCat = subCategory.getSuperCategory().getName();
        String subCat = subCategory.getName();
        Long id = subCategory.getId();

        controller.editSub("newName", subCat, superCat);

        assertTrue(subCategoryRepo.findOne(id).get().getName() == "newName");

    }


    public Boolean testSub(String name) {
        if (subCategoryRepo.findByName(name) == null) {
            return false;
        } else {
            return true;
        }
    }

    @Test
    public void addProduct() throws Exception {
        login("owner", "123");
        controller.addProductToCatalog("Test", "23.45", 1l, 12, 1, 1);


        assertTrue(testProcuct("Test"));

    }

    @Test
    public void editProduct() throws Exception {

        login("owner", "123");
        GSProduct product = catalog.findAll().iterator().next();
        ProductIdentifier id = product.getIdentifier();
        String strPrice = "12.34";


        controller.editProduct("newName", strPrice, 1l, 1l, 1, id);


        assertTrue(catalog.findOne(id).get().getName() == "newName");

    }


    public Boolean testProcuct(String name) {
        if (catalog.findByName(name) == null) {
            return false;
        } else {
            return true;
        }
    }

}