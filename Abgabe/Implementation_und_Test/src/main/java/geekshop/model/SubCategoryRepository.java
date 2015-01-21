package geekshop.model;

import org.salespointframework.core.SalespointRepository;

/**
 * Repository to store {@link geekshop.model.SubCategory}s.
 *
 * @author Sebastian Döring
 */

public interface SubCategoryRepository extends SalespointRepository<SubCategory, Long> {

    SubCategory findByName(String name);
    SubCategory findById(long id);
}
