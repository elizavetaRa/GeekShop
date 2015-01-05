package geekshop.model;

import org.salespointframework.core.SalespointRepository;

/**
 * Repository to store {@link geekshop.model.SuperCategory}s.
 *
 * @author Sebastian Döring
 */

public interface SuperCategoryRepository extends SalespointRepository<SuperCategory, Long> {

    SuperCategory findByName(String name);
}
