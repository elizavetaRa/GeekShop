package geekshop.model;

import geekshop.AbstractIntegrationTests;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test class for {@link SubCategoryRepository}.
 *
 * @author Sebastian Döring
 */

public class SubCategoryRepositoryTests extends AbstractIntegrationTests {

    @Autowired
    private SubCategoryRepository subCatRepo;
    @Autowired
    private SuperCategoryRepository superCatRepo;

    private SuperCategory superCategory;

    @Before
    public void setUp() {
        superCategory = new SuperCategory("superCat");
        superCatRepo.save(superCategory);
    }

    @Test
    public void testFindByName() {
        SubCategory subCat = new SubCategory("subCat", superCategory);
        subCatRepo.save(subCat);
        Assert.assertEquals("SubCategoryRepository should find a subcategory by name!", subCat, subCatRepo.findByName(subCat.getName()));
    }

    @Test
    public void testFindById() {
        SubCategory subCat = new SubCategory("subCat", superCategory);
        subCatRepo.save(subCat);
        Assert.assertEquals("SubCategoryRepository should find a subcategory by id!", subCat, subCatRepo.findById(subCat.getId()));
    }
}
