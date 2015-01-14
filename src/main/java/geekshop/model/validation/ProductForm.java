package geekshop.model.validation;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Validation class for form changing product data.
 *
 * @author Sebastian Döring
 */
public class ProductForm {

    @NotBlank(message = "Kein Name angegeben.")
    private String name;

    @NotNull(message = "Keine Artikelnummer angegeben.")
    @Pattern(regexp = "[1-9]\\d*", message = "Artikelnummer muss größer als 0 sein.")
    private String productNumber;

    @NotNull(message = "Kein Preis angegeben.")
    @Pattern(regexp = "[0-9]{1,3}(\\.?[0-9]{3})*,?[0-9]{0,2}\\s?€?", message = "Geben Sie den Preis in der Form x.xxx,xx an.")
    private String price;

    @NotNull(message = "Minimale Stückzahl nicht angegeben.")
    @Pattern(regexp = "\\d+", message = "Minimale Stückzahl muss eine positive Zahl sein.")
    private String minQuantity;

    @NotNull(message = "Stückzahl nicht angegeben.")
    @Pattern(regexp = "\\d+", message = "Stückzahl muss eine positive Zahl sein.")
    private String quantity;

    @NotEmpty(message = "Keine Kategorie angegeben.")
    private String subCategory;


    public ProductForm() {
    }

    public ProductForm(String name, String productNumber, String price, String minQuantity, String quantity, String subCategory) {
        this.name = name;
        this.productNumber = productNumber;
        this.price = price;
        this.minQuantity = minQuantity;
        this.quantity = quantity;
        this.subCategory = subCategory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(String minQuantity) {
        this.minQuantity = minQuantity;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }
}
