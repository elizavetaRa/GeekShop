package geekshop.model;

import org.joda.money.Money;
import org.joda.money.format.MoneyFormatter;
import org.joda.money.format.MoneyFormatterBuilder;
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

    private long productNumber;
    @ManyToOne
    private SubCategory subCategory;

    private Boolean inRange;

    @Deprecated
    protected GSProduct() {
    }

    public GSProduct(long productNumber, String name, Money price, SubCategory subCategory) {
        super (name, price, Units.METRIC);
        Assert.notNull(subCategory, "SubCategory must be not null.");
        this.productNumber = productNumber;
        this.subCategory = subCategory;
        this.inRange = true;
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(SubCategory subCategory){
        this.subCategory = subCategory;
    }

    public long getProductNumber() {
        return productNumber;
    }

    public double getPriceDouble() {
        String[] array;
        String temp;
        temp = getPrice().toString();
        array = temp.split(" ");
        temp = array[1];
        double value = Double.parseDouble(temp);
        return value;
    }

    public Boolean isInRange() {
        return inRange;
    }

    public void setInRange(Boolean inRange) {
        this.inRange = inRange;
    }

    public static String moneyToString(Money money) {
        MoneyFormatter moneyFormatter = new MoneyFormatterBuilder().appendAmountLocalized().appendLiteral(" ").appendCurrencySymbolLocalized().toFormatter();
        return moneyFormatter.print(money);
    }
}
