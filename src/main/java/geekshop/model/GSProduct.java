package geekshop.model;

/*
 * Created by Marc on 02.12.2014.
 */

import org.joda.money.Money;
import org.salespointframework.catalog.Product;
import org.salespointframework.quantity.Units;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * An extension of {@link org.salespointframework.catalog.Product} extended by a {@link geekshop.model.SubCategory} the product is related to and a unique product number.
 *
 * @author Marcus Kammerdiener
 * @author Sebastian D&ouml;ring
 */

@Entity
public class GSProduct extends Product{

    private int productNumber;
    @ManyToOne
    private SubCategory subCategory;

    @Deprecated
    protected GSProduct() {
    }

    public GSProduct (String name, Money price, SubCategory subCategory, int productNumber) {
        super (name, price, Units.METRIC);
        Assert.notNull(subCategory, "SubCategory must be not null.");
        this.subCategory = subCategory;
        this.productNumber = productNumber;
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public int getProductNumber() {
        return productNumber;
    }

    public String productNumberToString(int geekID) {
        return geekID + "";
    }

    public String getStringPrice() {
        String[] array;
        String temp;
        temp = getPrice().toString();
        array = temp.split(" ");
        temp = array[1]+ " " +array[0];
        return temp;
    }

}
