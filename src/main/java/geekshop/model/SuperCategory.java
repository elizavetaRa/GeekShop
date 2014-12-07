package geekshop.model;

/*
 * Created by Marc on 02.12.2014.
 */

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

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
    private Set<SubCategory> subCategories;

    @Deprecated
    protected SuperCategory() {
    }

    public SuperCategory(String name) {
        this.name = name;
        this.subCategories = new HashSet<SubCategory>();
    }

    public String getName() {
        return name;
    }

    public Set<SubCategory> getSubCategories() {
        return subCategories;
    }

    public boolean addSubCategory(SubCategory subCategory) {
        return subCategories.add(subCategory);
    }
}
