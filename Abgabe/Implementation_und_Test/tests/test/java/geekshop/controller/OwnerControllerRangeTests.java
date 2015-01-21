package geekshop.controller;

import geekshop.AbstractWebIntegrationTests;
import geekshop.model.*;
import geekshop.model.validation.ProductForm;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.ProductIdentifier;
import org.salespointframework.inventory.Inventory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;

import static org.junit.Assert.assertEquals;
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
    @Autowired
    Inventory<GSInventoryItem> inventory;
    @Autowired
    private Validator validator;

    private Model model;
    private WebDataBinder binder;


    @Before
    public void setUp() {
        login("owner", "123");

        model = new ExtendedModelMap();

        ProductForm productForm = new ProductForm();
        binder = new WebDataBinder(productForm);
        binder.setValidator(validator);
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
        return superCategoryRepo.findByName(name) != null;
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

        assertTrue(subCategoryRepo.findOne(id).get().getName().equals("newName"));

    }


    public Boolean testSub(String name) {
        return subCategoryRepo.findByName(name) != null;
    }

    @Test
    public void addProduct() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/range/addproduct");
        request.addParameter("name", "Test");
        request.addParameter("productNumber", "123");
        request.addParameter("price", "23.45");
        request.addParameter("minQuantity", "11");
        request.addParameter("quantity", "12");
        request.addParameter("subCategory", "Informatik");
        binder.bind(new MutablePropertyValues(request.getParameterMap()));
        binder.getValidator().validate(binder.getTarget(), binder.getBindingResult());


        controller.addProductToCatalog(model, (ProductForm) binder.getTarget(), binder.getBindingResult());


        assertTrue(testProcuct("Test"));

    }

    @Test
    public void editProduct() throws Exception {

        GSProduct product = catalog.findAll().iterator().next();
        ProductIdentifier id = product.getIdentifier();
        GSInventoryItem item = inventory.findByProductIdentifier(id).get();

        String strPrice = "12,34 €";

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/range/editproduct/" + product.getId());
        request.addParameter("name", "newName");
        request.addParameter("productNumber", String.valueOf(product.getProductNumber()));
        request.addParameter("price", strPrice);
        request.addParameter("minQuantity", String.valueOf(item.getMinimalQuantity().getAmount().longValue()));
        request.addParameter("quantity", String.valueOf(item.getQuantity().getAmount().longValue()));
        request.addParameter("subCategory", product.getSubCategory().getName());
        binder.bind(new MutablePropertyValues(request.getParameterMap()));
        binder.getValidator().validate(binder.getTarget(), binder.getBindingResult());


        controller.editProduct(model, id, (ProductForm) binder.getTarget(), binder.getBindingResult());


        assertEquals(product.getName(), "newName");
        assertEquals(product.getPrice(), Money.of(CurrencyUnit.EUR, 12.34D));
    }


    public Boolean testProcuct(String name) {
        return catalog.findByName(name) != null;
    }

}