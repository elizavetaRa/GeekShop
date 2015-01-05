package geekshop.model;

import org.salespointframework.core.SalespointRepository;

/**
 * Repository to store {@link geekshop.model.SuperCategory}s.
 *
 * @author Sebastian D&ouml;ring
 */

public interface SuperCategoryRepository extends SalespointRepository<SuperCategory, Long> {

    SuperCategory findByName(String name);
}
