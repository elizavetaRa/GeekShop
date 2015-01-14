package geekshop.model.validation;

import geekshop.model.User;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Date;

/**
 * Determines whether the given String value of {@link DateOfBirth} represents a valid date arranged in the common order of the German-speaking area.
 */
public class DateOfBirthValidator implements ConstraintValidator<DateOfBirth, String> {

    public void initialize(DateOfBirth dateOfBirth) {
    }

    /**
     * @return {@literal true} if the given String represents a date before now in which day, month and year are formatted in the common order of the German-speaking area,
     * e. g. {@literal 14.03.95} or {@literal 14.03.1995}. Permitted as separators are white-space character, {@literal .}, {@literal -} and {@literal /}.
     */
    public boolean isValid(String date, ConstraintValidatorContext constraintContext) {

        if (date == null || date.isEmpty())
            return true;

        Date dateOfBirth = User.strToDate(date.trim());
        return dateOfBirth != null && dateOfBirth.before(new Date());
    }

}
