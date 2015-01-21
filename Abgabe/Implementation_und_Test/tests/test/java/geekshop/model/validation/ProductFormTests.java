package geekshop.model.validation;

import geekshop.AbstractIntegrationTests;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link geekshop.model.validation.ProductForm}.
 *
 * @author Sebastian Döring
 */

public class ProductFormTests extends AbstractIntegrationTests {

    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testWithInvalidInput() {
        ProductForm pf;
        Set<ConstraintViolation<ProductForm>> constrViolations;

        pf = new ProductForm(
                null, null, null, null, null, null
        );
        constrViolations = validator.validate(pf);
        assertEquals(6, constrViolations.size());

        pf = new ProductForm(
                "", "", "", "", "", ""
        );
        constrViolations = validator.validate(pf);
        assertEquals(6, constrViolations.size());

        pf = new ProductForm(
                "a", "b", "c", "d", "e", "f"
        );
        constrViolations = validator.validate(pf);
        assertEquals(4, constrViolations.size());

        pf = new ProductForm(
                "Pro Duct", "-1", "-1", "-1", "-1", "1"
        );
        constrViolations = validator.validate(pf);
        assertEquals(4, constrViolations.size());

        pf = new ProductForm(
                "Pro Duct", "0", "0", "0", "0", "Informatik"
        );
        constrViolations = validator.validate(pf);
        assertEquals(1, constrViolations.size());
    }

    @Test
    public void testWithValidInput() {
        ProductForm pf;
        Set<ConstraintViolation<ProductForm>> constrViolations;

        pf = new ProductForm(
                "Pro Duct", "1", "0,00", "0", "0", "Informatik"
        );
        constrViolations = validator.validate(pf);
        assertTrue(constrViolations.isEmpty());

        pf = new ProductForm(
                "Pro Duct", "2", "1,23 €", "2", "1", "Informatik"
        );
        constrViolations = validator.validate(pf);
        assertTrue(constrViolations.isEmpty());

        pf = new ProductForm(
                "Pro Duct", "2", "1.000€", "2", "1", "Informatik"
        );
        constrViolations = validator.validate(pf);
        assertTrue(constrViolations.isEmpty());

        pf = new ProductForm(
                "Pro Duct Tape", "2", "1.000.000.000,99", "123", "2123456", "Informatik"
        );
        constrViolations = validator.validate(pf);
        assertTrue(constrViolations.isEmpty());
    }
}
