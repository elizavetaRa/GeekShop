package geekshop.model;

import geekshop.AbstractIntegrationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test class for {@link SuperCategoryRepository}.
 *
 * @author Sebastian Döring
 */

public class SuperCategoryRepositoryTests extends AbstractIntegrationTests {

    @Autowired
    private SuperCategoryRepository superCatRepo;


    @Test
    public void testFindByName() {
        SuperCategory superCat = new SuperCategory("superCat");
        superCatRepo.save(superCat);
        Assert.assertEquals("SuperCategoryRepository should find a supercategory by name!", superCat, superCatRepo.findByName(superCat.getName()));
    }
}
