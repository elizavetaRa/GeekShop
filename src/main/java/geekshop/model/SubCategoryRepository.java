package geekshop.model;

/*
 * Created by Basti on 07.12.2014.
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
