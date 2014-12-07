package geekshop.model;

/*
 * Created by h4llow3En on 20/11/14.
 */

import org.salespointframework.core.SalespointRepository;

/**
 * Repository to store {@link geekshop.model.SubCategory}s.
 *
 * @author Sebastian D&ouml;ring
 */

public interface SubCategoryRepository extends SalespointRepository<SubCategory, Long> {

    User findByName(String name);
}
