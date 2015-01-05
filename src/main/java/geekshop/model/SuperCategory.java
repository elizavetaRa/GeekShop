package geekshop.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.LinkedList;
import java.util.List;

/**
 * Class representing a {@link SuperCategory} which contains {@link SubCategory}s.
 *
 * @author Marcus Kammerdiener
 * @author Sebastian D&ouml;ring
 */

@Entity
public class SuperCategory {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    @OneToMany
    private List<SubCategory> subCategories;

    @Deprecated
    protected SuperCategory() {
    }

    /**
     * Creates a new {@link SuperCategory} with the given name.
     */
    public SuperCategory(String name) {
        this.name = name;
        this.subCategories = new LinkedList<SubCategory>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public List<SubCategory> getSubCategories() {
        return subCategories;
    }

    public boolean addSubCategory(SubCategory subCategory) {
        return subCategories.add(subCategory);
    }

    public Long getId() {
        return id;
    }
}
