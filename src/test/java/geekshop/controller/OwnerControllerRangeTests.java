package geekshop.controller;

import geekshop.AbstractWebIntegrationTests;
import geekshop.model.*;
import org.junit.Before;
import org.junit.Test;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.ProductIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.Assert.assertTrue;

public class OwnerControllerRangeTests extends AbstractWebIntegrationTests {

    @Autowired
    OwnerController controller;

    @Autowired
    SubCategoryRepository subCategoryRepo;
    @Autowired
    SuperCategoryRepository superCategoryRepo;
    @Autowired
    Catalog<GSProduct> catalog;

    private Model model;


    @Before
    public void setUp() {
        login("owner", "123");

        model = new ExtendedModelMap();
    }

    @Test
    public void addSuperCat() throws Exception {
        controller.addSuperCategory(model, "Test");


        assertTrue(testSuper("Test"));

    }

    @Test
    public void editSuper() throws Exception {

        SuperCategory superCategory = superCategoryRepo.findAll().iterator().next();
        String superCat = superCategory.getName();
        Long id = superCategory.getId();

        controller.editSuper(model, superCat, "newName");

        assertTrue(superCategoryRepo.findOne(id).get().getName().equals("newName"));
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
        String superCategory = superCategoryRepo.findAll().iterator().next().getName();
        controller.addSubCategory(model, "Test", superCategory);


        assertTrue(testSub("Test"));

    }

    @Test
    public void editSub() throws Exception {

        SubCategory subCategory = subCategoryRepo.findAll().iterator().next();
        String superCat = subCategory.getSuperCategory().getName();
        String subCat = subCategory.getName();
        Long id = subCategory.getId();

        controller.editSub(model, subCat, "newName", superCat);

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
        controller.addProductToCatalog("Test", "23.45", 1l, 12, 1, 1);


        assertTrue(testProcuct("Test"));

    }

    @Test
    public void editProduct() throws Exception {

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