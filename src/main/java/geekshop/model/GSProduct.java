package geekshop.model;

import org.joda.money.Money;
import org.salespointframework.catalog.Product;
import org.salespointframework.quantity.Units;
import org.springframework.util.Assert;

/**
 * Created by Marc on 02.12.2014.
 */
public class GSProduct extends Product{

    private SubCategory subCategory;

    @Deprecated
    protected GSProduct() {
    }

    public GSProduct (String name, Money price, SubCategory subCategory) {
        super (name, price, Units.METRIC);
        Assert.notNull(subCategory, "SubCategory must be not null.");
        this.subCategory = subCategory;
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }


}
