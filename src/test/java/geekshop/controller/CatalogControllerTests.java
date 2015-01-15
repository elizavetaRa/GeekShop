package geekshop.controller;

import geekshop.AbstractWebIntegrationTests;
import geekshop.model.*;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.quantity.Units;
import org.salespointframework.useraccount.AuthenticationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class CatalogControllerTests extends AbstractWebIntegrationTests {

    @Autowired
    private CatalogController controller;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private Inventory inventory;
    @Autowired
    private Catalog<GSProduct> catalog;
    @Autowired
    private SubCategoryRepository subCatRepo;
    @Autowired
    private SuperCategoryRepository superCatRepo;


    private Model model;
    private User owner;

    private GSProduct prod1;
    private GSProduct prod2;
    private GSProduct prod3;
    private GSProduct prod4;
    private GSProduct prod5;
    private GSProduct prod6;
    private GSProduct prod7;
    private GSProduct prod8;
    private GSProduct prod9;
    private GSProduct prod10;
    private GSProduct prod11;


    @Before
    public void setUp() {

        super.setUp();

        model = new ExtendedModelMap();

        login("owner", "123");
        owner = userRepo.findByUserAccount(authManager.getCurrentUser().get());

        SuperCategory superCategory1 = new SuperCategory("superCat1");
        SuperCategory superCategory2 = new SuperCategory("superCat2");
        superCatRepo.save(superCategory1);
        superCatRepo.save(superCategory2);

        SubCategory subCategory1 = new SubCategory("subCat1", superCategory1);
        SubCategory subCategory2 = new SubCategory("subCat2", superCategory1);
        SubCategory subCategory3 = new SubCategory("subCat3", superCategory2);
        SubCategory subCategory4 = new SubCategory("subCat4", superCategory2);
        subCatRepo.save(subCategory1);
        subCatRepo.save(subCategory2);
        subCatRepo.save(subCategory3);
        subCatRepo.save(subCategory4);

        prod1 = new GSProduct(100, "Microcontroller Manschettenknöpfe", Money.of(CurrencyUnit.EUR, 1D), subCategory1);
        prod2 = new GSProduct(102, "T-Shirt „There is no place like 127.0.0.1“", Money.of(CurrencyUnit.EUR, 2D), subCategory3);
        prod3 = new GSProduct(101, "T-Shirt „Be rational. Get real.“", Money.of(CurrencyUnit.EUR, 2D), subCategory2);
        prod4 = new GSProduct(103, "T-Shirt „WANTED. Dead and Alive. Schrödinger's Cat“", Money.of(CurrencyUnit.EUR, 934.21D), subCategory2);
        prod5 = new GSProduct(106, "Selbstumrührender Becher", Money.of(CurrencyUnit.EUR, 111.21D), subCategory1);
        prod6 = new GSProduct(104, "Star Wars Essstäbchen", Money.of(CurrencyUnit.EUR, 11.21D), subCategory1);
        prod7 = new GSProduct(105, "Wäschefalter aus The Big Bang Theory", Money.of(CurrencyUnit.EUR, 99.21D), subCategory1);
        prod8 = new GSProduct(107, "USB-Staubsauger", Money.of(CurrencyUnit.EUR, 0.21D), subCategory4);
        prod9 = new GSProduct(108, "Aufkleber „This is NOT a touchscreen!“", Money.of(CurrencyUnit.EUR, 33.21D), subCategory4);
        prod10 = new GSProduct(109, "Aufkleber „Enter any 11-digit prime number to continue.“", Money.of(CurrencyUnit.EUR, 109751.21D), subCategory4);
        prod11 = new GSProduct(110, "Aufkleber", Money.of(CurrencyUnit.EUR, 100D), subCategory4);

        catalog.save(Arrays.asList(prod1, prod2, prod3, prod4, prod5, prod6, prod7, prod8, prod9, prod10, prod11));

        GSInventoryItem item1 = new GSInventoryItem(prod1, Units.ONE, Units.ZERO);
        GSInventoryItem item2 = new GSInventoryItem(prod2, Units.ONE, Units.ZERO);
        GSInventoryItem item3 = new GSInventoryItem(prod3, Units.ONE, Units.ZERO);
        GSInventoryItem item4 = new GSInventoryItem(prod4, Units.ONE, Units.ZERO);
        GSInventoryItem item5 = new GSInventoryItem(prod5, Units.ONE, Units.ZERO);
        GSInventoryItem item6 = new GSInventoryItem(prod6, Units.ONE, Units.ZERO);
        GSInventoryItem item7 = new GSInventoryItem(prod7, Units.ONE, Units.ZERO);
        GSInventoryItem item8 = new GSInventoryItem(prod8, Units.ONE, Units.ZERO);
        GSInventoryItem item9 = new GSInventoryItem(prod9, Units.ONE, Units.ZERO);
        GSInventoryItem item10 = new GSInventoryItem(prod10, Units.ONE, Units.ZERO);
        GSInventoryItem item11 = new GSInventoryItem(prod11, Units.ONE, Units.ZERO);

        inventory.save(Arrays.asList(item1, item2, item3, item4, item5, item6, item7, item8, item9, item10, item11));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testParameterlessProductSearch() {
        controller.searchEntryByName(model, Optional.of(owner.getUserAccount()), null, null, null);

        List<GSProduct> actual = (List<GSProduct>) model.asMap().get("catalog");

        for (GSProduct prod : catalog.findAll()) {

            assertThat((Iterable<GSProduct>) model.asMap().get("catalog"), hasItem(prod));
        }
        System.out.println(actual.size());
        assertTrue(actual.size() == 11);


    }

    @Test
    @SuppressWarnings("unchecked")
    public void testProductSearch() {
        controller.searchEntryByName(model, Optional.of(owner.getUserAccount()), "Shirt", null, null);

        List<GSProduct> actual = (List<GSProduct>) model.asMap().get("catalog");

        for (GSProduct prod : catalog.findByName("Shirt")) {
            assertThat((Iterable<GSProduct>) model.asMap().get("catalog"), hasItem(prod));
        }

        assertTrue(actual.size() == 3);

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCategoryView() {
        controller.searchEntryByName(model, Optional.of(owner.getUserAccount()), null, null, "subCat1");

        for (GSProduct prod : catalog.findByCategory("subCat1")) {
            assertThat((Iterable<GSProduct>) model.asMap().get("catalog"), hasItem(prod));
        }

        List<GSProduct> actual = (List<GSProduct>) model.asMap().get("catalog");

        assertTrue(actual.size() == 4);

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testParameterlessSortByProductNumber() {
        controller.searchEntryByName(model, Optional.of(owner.getUserAccount()), null, "prodnum", null);

        List<GSProduct> actual = (List<GSProduct>) model.asMap().get("catalog");

        for (GSProduct prod : catalog.findAll()) {

            assertThat((Iterable<GSProduct>) model.asMap().get("catalog"), hasItem(prod));
        }

        for (int i = 0; i < actual.size(); i++) {
            if (i > 0)
                assertTrue(actual.get(i).getProductNumber() > actual.get(i - 1).getProductNumber());
            else
                assertTrue(actual.get(i).getProductNumber() < actual.get(i + 1).getProductNumber());
        }
        assertTrue(actual.size() == 11);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testParameterlessSortByPriceAscending() {
        controller.searchEntryByName(model, Optional.of(owner.getUserAccount()), null, "priceasc", null);

        List<GSProduct> actual = (List<GSProduct>) model.asMap().get("catalog");

        for (GSProduct prod : catalog.findAll()) {

            assertThat((Iterable<GSProduct>) model.asMap().get("catalog"), hasItem(prod));
        }

        for (int i = 0; i < actual.size(); i++) {
            if (i > 0)
                assertTrue(GSProduct.moneyToString(actual.get(i).getPrice()).compareTo(GSProduct.moneyToString(actual.get(i - 1).getPrice())) == -1);
            else
                assertTrue(GSProduct.moneyToString(actual.get(i).getPrice()).compareTo(GSProduct.moneyToString(actual.get(i + 1).getPrice())) == 1);
        }

        assertTrue(actual.size() == 11);


    }

    @Test
    @SuppressWarnings("unchecked")
    public void testParameterlessSortByPriceDescending() {
        controller.searchEntryByName(model, Optional.of(owner.getUserAccount()), null, "pricedesc", null);

        List<GSProduct> actual = (List<GSProduct>) model.asMap().get("catalog");

        for (GSProduct prod : catalog.findAll()) {

            assertThat((Iterable<GSProduct>) model.asMap().get("catalog"), hasItem(prod));
        }

        for (int i = 0; i < actual.size(); i++) {
            if (i > 0)
                assertTrue(GSProduct.moneyToString(actual.get(i).getPrice()).compareTo(GSProduct.moneyToString(actual.get(i - 1).getPrice())) == 1);
            else
                assertTrue(GSProduct.moneyToString(actual.get(i).getPrice()).compareTo(GSProduct.moneyToString(actual.get(i + 1).getPrice())) == -1);
        }

        assertTrue(actual.size() == 11);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSearchedSortByProductNumber() {
        controller.searchEntryByName(model, Optional.of(owner.getUserAccount()), "Aufkleber", "prodnum", null);

        List<GSProduct> actual = (List<GSProduct>) model.asMap().get("catalog");

        for (GSProduct prod : catalog.findByName("Aufkleber")) {

            assertThat((Iterable<GSProduct>) model.asMap().get("catalog"), hasItem(prod));
        }

        for (int i = 0; i < actual.size(); i++) {
            if (i > 0)
                assertTrue(actual.get(i).getProductNumber() > actual.get(i - 1).getProductNumber());
            else
                assertTrue(actual.get(i).getProductNumber() < actual.get(i + 1).getProductNumber());
        }

        assertTrue(actual.size() == 3);

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSearchedSortByPriceAscending() {
        controller.searchEntryByName(model, Optional.of(owner.getUserAccount()), "Aufkleber", "priceasc", null);

        List<GSProduct> actual = (List<GSProduct>) model.asMap().get("catalog");

        for (GSProduct prod : catalog.findByName("Aufkleber")) {

            assertThat((Iterable<GSProduct>) model.asMap().get("catalog"), hasItem(prod));
        }

        for (int i = 0; i < actual.size(); i++) {
            if (i > 0)
                assertTrue(GSProduct.moneyToString(actual.get(i).getPrice()).compareTo(GSProduct.moneyToString(actual.get(i - 1).getPrice())) == -1);
            else
                assertTrue(GSProduct.moneyToString(actual.get(i).getPrice()).compareTo(GSProduct.moneyToString(actual.get(i + 1).getPrice())) == 1);
        }

        assertTrue(actual.size() == 3);

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSearchedSortByPriceDescending() {
        controller.searchEntryByName(model, Optional.of(owner.getUserAccount()), "Aufkleber", "pricedesc", null);

        List<GSProduct> actual = (List<GSProduct>) model.asMap().get("catalog");

        for (GSProduct prod : catalog.findByName("Aufkleber")) {

            assertThat((Iterable<GSProduct>) model.asMap().get("catalog"), hasItem(prod));
        }

        for (int i = 0; i < actual.size(); i++) {
            if (i > 0)
                assertTrue(GSProduct.moneyToString(actual.get(i).getPrice()).compareTo(GSProduct.moneyToString(actual.get(i - 1).getPrice())) == 1);
            else
                assertTrue(GSProduct.moneyToString(actual.get(i).getPrice()).compareTo(GSProduct.moneyToString(actual.get(i + 1).getPrice())) == -1);
        }

        assertTrue(actual.size() == 3);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCategorySortByProductNumber() {
        controller.searchEntryByName(model, Optional.of(owner.getUserAccount()), null, "prodnum", "subCat1");

        List<GSProduct> actual = (List<GSProduct>) model.asMap().get("catalog");

        for (GSProduct prod : catalog.findByCategory("subCat1")) {

            assertThat((Iterable<GSProduct>) model.asMap().get("catalog"), hasItem(prod));
        }

        for (int i = 0; i < actual.size(); i++) {
            if (i > 0)
                assertTrue(actual.get(i).getProductNumber() > actual.get(i - 1).getProductNumber());
            else
                assertTrue(actual.get(i).getProductNumber() < actual.get(i + 1).getProductNumber());
        }

        assertTrue(actual.size() == 4);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCategorySortByPriceAscending() {
        controller.searchEntryByName(model, Optional.of(owner.getUserAccount()), null, "priceasc", "subCat1");

        List<GSProduct> actual = (List<GSProduct>) model.asMap().get("catalog");

        for (GSProduct prod : catalog.findByCategory("subCat1")) {

            assertThat((Iterable<GSProduct>) model.asMap().get("catalog"), hasItem(prod));
        }

        for (int i = 0; i < actual.size(); i++) {
            if (i > 0)
                assertTrue(GSProduct.moneyToString(actual.get(i).getPrice()).compareTo(GSProduct.moneyToString(actual.get(i - 1).getPrice())) == -1);
            else
                assertTrue(GSProduct.moneyToString(actual.get(i).getPrice()).compareTo(GSProduct.moneyToString(actual.get(i + 1).getPrice())) == 1);
        }

        assertTrue(actual.size() == 4);

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCategorySortByPriceDescending() {
        controller.searchEntryByName(model, Optional.of(owner.getUserAccount()), null, "pricedesc", "subCat1");

        List<GSProduct> actual = (List<GSProduct>) model.asMap().get("catalog");

        for (GSProduct prod : catalog.findByCategory("subCat1")) {

            assertThat((Iterable<GSProduct>) model.asMap().get("catalog"), hasItem(prod));
        }

        for (int i = 0; i < actual.size(); i++) {
            if (i > 0)
                assertTrue(GSProduct.moneyToString(actual.get(i).getPrice()).compareTo(GSProduct.moneyToString(actual.get(i - 1).getPrice())) == 1);
            else
                assertTrue(GSProduct.moneyToString(actual.get(i).getPrice()).compareTo(GSProduct.moneyToString(actual.get(i + 1).getPrice())) == -1);
        }

        assertTrue(actual.size() == 4);

    }
}
