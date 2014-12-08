package geekshop.model;

/*
 * Created by Created by Basti on 07.12.2014.
 */

import org.salespointframework.core.SalespointRepository;

/**
 * Repository to store {@link geekshop.model.SuperCategory}s.
 *
 * @author Sebastian D&ouml;ring
 */

public interface SuperCategoryRepository extends SalespointRepository<SuperCategory, Long> {

    User findByName(String name);
}
