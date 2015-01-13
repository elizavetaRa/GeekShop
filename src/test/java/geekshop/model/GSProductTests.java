package geekshop.model;

import geekshop.AbstractIntegrationTests;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class GSProductTests extends AbstractIntegrationTests  {

    @Autowired
    private SubCategoryRepository subCatRepo;
    @Autowired
    private SuperCategoryRepository superCatRepo;


    private GSProduct prod1;
    private GSProduct prod2;
    private GSProduct prod3;


    @Before
    public void setUp() {

        SuperCategory superCategory = new SuperCategory("superCat");
        superCatRepo.save(superCategory);
        SubCategory subCategory = new SubCategory("subCat", superCategory);
        subCatRepo.save(subCategory);
        prod1 = new GSProduct(100, "test1", Money.of(CurrencyUnit.EUR, 1D), subCategory);
        prod2 = new GSProduct(101, "test2", Money.of(CurrencyUnit.EUR, 2D), subCategory);
        prod3 = new GSProduct(102, "test3", Money.of(CurrencyUnit.EUR, 3.21D), subCategory);
    }

    @Test
    public void testConstructor() {
        Assert.assertEquals("ProductNumber incorrect", 100, prod1.getProductNumber());
        Assert.assertEquals("Name is incorrect", "test1", prod1.getName());
        Assert.assertEquals("Price is incorrect", Money.of(CurrencyUnit.EUR, 1D), prod1.getPrice());
        Assert.assertEquals("SubCategory is incorrect", subCatRepo.findByName("subCat"), prod1.getSubCategory());
    }

    @Test
    public void testMoneyToString() {
        Assert.assertEquals("Converted value is wrong", "2,00 €", GSProduct.moneyToString(prod2.getPrice()));
        Assert.assertEquals("Converted value is wrong", "3,21 €", GSProduct.moneyToString(prod3.getPrice()));
    }
}
