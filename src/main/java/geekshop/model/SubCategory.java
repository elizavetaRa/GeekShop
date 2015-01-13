package geekshop.model;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Class representing a {@link SubCategory} which contains {@link org.salespointframework.catalog.Product}s.
 *
 * @author Marcus Kammerdiener
 * @author Sebastian Döring
 */

@Entity
public class SubCategory {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    @ManyToOne
    private SuperCategory superCategory;
    @OneToMany
    private List<GSProduct> products;

    @Deprecated
    protected SubCategory() {
    }

    /**
     * Creates a new {@link SubCategory} with the given name and the {@link SuperCategory} this subcategory is related to.
     */
    public SubCategory(String name, SuperCategory supc) {
        this.name = name;
        this.superCategory = supc;
        this.products = new LinkedList<GSProduct>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SuperCategory getSuperCategory() {
        return superCategory;
    }

    public void setSuperCategory(SuperCategory superCategory) {
        this.superCategory = superCategory;
    }

    public List<GSProduct> getProducts() {
        return products;
    }

    /**
     * Adds a {@Link GSProduct} to the {@Link SubCategory}.
     */

    public boolean addProduct(GSProduct product) {
        return products.add(product);
    }

    public Long getId() {
        return id;
    }
}
