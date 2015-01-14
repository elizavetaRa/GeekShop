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
 * Test class for {@link geekshop.model.validation.SetRulesForm}.
 *
 * @author Sebastian Döring
 */

public class SetRulesFormTests extends AbstractIntegrationTests {

    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testWithInvalidInput() {
        SetRulesForm srf;
        Set<ConstraintViolation<SetRulesForm>> constrViolations;

        srf = new SetRulesForm();
        srf.setMinLength("-1");
        constrViolations = validator.validate(srf);
        assertEquals(1, constrViolations.size());

        srf = new SetRulesForm();
        srf.setMinLength("0");
        constrViolations = validator.validate(srf);
        assertEquals(1, constrViolations.size());

        srf = new SetRulesForm();
        srf.setMinLength("5");
        constrViolations = validator.validate(srf);
        assertEquals(1, constrViolations.size());

    }

    @Test
    public void testWithValidInput() {
        SetRulesForm srf;
        Set<ConstraintViolation<SetRulesForm>> constrViolations;

        srf = new SetRulesForm();
        srf.setMinLength("6");
        constrViolations = validator.validate(srf);
        assertTrue(constrViolations.isEmpty());

        srf = new SetRulesForm();
        srf.setMinLength("120");
        constrViolations = validator.validate(srf);
        assertTrue(constrViolations.isEmpty());
    }
}
