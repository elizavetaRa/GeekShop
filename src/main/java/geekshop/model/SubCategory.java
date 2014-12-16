package geekshop.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Class representing a {@link SubCategory} which contains {@link org.salespointframework.catalog.Product}s.
 *
 * @author Marcus Kammerdiener
 * @author Sebastian D&ouml;ring
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
    private Set<GSProduct> products;

    @Deprecated
    protected SubCategory() {
    }

    public SubCategory(String name, SuperCategory supc) {
        this.name = name;
        this.superCategory = supc;
        this.products = new HashSet<GSProduct>();
    }

    public String getName() {
        return name;
    }

    public SuperCategory getSuperCategory() {
        return superCategory;
    }

    public Set<GSProduct> getProducts() {
        return products;
    }

    public boolean addProduct(GSProduct product) {
        return products.add(product);
    }
}
