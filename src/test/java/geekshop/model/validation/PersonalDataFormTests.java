package geekshop.model.validation;

import geekshop.AbstractIntegrationTests;
import geekshop.model.Gender;
import geekshop.model.MaritalStatus;
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
 * Test class for {@link geekshop.model.validation.PersonalDataForm}.
 *
 * @author Sebastian Döring
 */

public class PersonalDataFormTests extends AbstractIntegrationTests {

    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testWithInvalidInput() {
        PersonalDataForm pdf;
        Set<ConstraintViolation<PersonalDataForm>> constrViolations;

        pdf = new PersonalDataForm(
                null, null, null, null, null, null, null, null, null, null, null, null
        );
        constrViolations = validator.validate(pdf);
        assertEquals(12, constrViolations.size());

        pdf = new PersonalDataForm(
                "", "", "", "", Gender.SOMETHING_ELSE, "", MaritalStatus.UNKNOWN, "", "", "", "", ""
        );
        constrViolations = validator.validate(pdf);
        assertEquals(10, constrViolations.size());

        pdf = new PersonalDataForm(
                "a", "b", "c", "d", Gender.MALE, "e", MaritalStatus.UNKNOWN, "f", "g", "h", "i", "j"
        );
        constrViolations = validator.validate(pdf);
        assertEquals(6, constrViolations.size());

        pdf = new PersonalDataForm(
                "Täst'sche", "Persönlichkeit", "cc", "d@", Gender.MALE, "123.123.123", MaritalStatus.UNKNOWN, "1", "Str.", "1", "1", "Place"
        );
        constrViolations = validator.validate(pdf);
        assertEquals(5, constrViolations.size());

        pdf = new PersonalDataForm(
                "Täst'sche", "Persönlichkeit", "ccc", "d@d", Gender.MALE, "1.1.1", MaritalStatus.UNKNOWN, "123456", "Str.", "1", "12345", "Place"
        );
        constrViolations = validator.validate(pdf);
        assertEquals(2, constrViolations.size());

        pdf = new PersonalDataForm(
                "Täst'sche", "Persönlichkeit", "c cc", "d@d", Gender.MALE, "1.1.1", MaritalStatus.UNKNOWN, "12345678", "Str.", "1", "12345", "Place"
        );
        constrViolations = validator.validate(pdf);
        assertEquals(2, constrViolations.size());

        pdf = new PersonalDataForm(
                "Täst'sche", "Persönlichkeit", "täst", "dd@d.de", Gender.MALE, "01.01.2345", MaritalStatus.UNKNOWN, "12345678", "Str.", "1", "12345", "Place"
        );
        constrViolations = validator.validate(pdf);
        assertEquals(1, constrViolations.size());
    }

    @Test
    public void testWithValidInput() {
        PersonalDataForm pdf;
        Set<ConstraintViolation<PersonalDataForm>> constrViolations;

        pdf = new PersonalDataForm(
                "Täst'sche", "Persönlichkeit", "täst", "dd@d.de", Gender.MALE, "1.1.12", MaritalStatus.UNKNOWN, "12345678", "Str.", "1", "12345", "Place"
        );
        constrViolations = validator.validate(pdf);
        assertTrue(constrViolations.isEmpty());

        pdf = new PersonalDataForm(
                "Täst'sche", "Persönlichkeit", "täst", "dd@d.de", Gender.MALE, "12-12-12", MaritalStatus.UNKNOWN, "12345678", "Str.", "1", "12345", "Place"
        );
        constrViolations = validator.validate(pdf);
        assertTrue(constrViolations.isEmpty());

        pdf = new PersonalDataForm(
                "Täst'sche", "Persönlichkeit", "täst", "dd@d.de", Gender.MALE, "11 11 11", MaritalStatus.UNKNOWN, "12345678", "Str.", "1", "12345", "Place"
        );
        constrViolations = validator.validate(pdf);
        assertTrue(constrViolations.isEmpty());

        pdf = new PersonalDataForm(
                "Täst'sche", "Persönlichkeit", "täst", "dd@d.de", Gender.MALE, "11/11/1911", MaritalStatus.UNKNOWN, "12345678", "Str.", "1", "12345", "Place"
        );
        constrViolations = validator.validate(pdf);
        assertTrue(constrViolations.isEmpty());
    }
}
