package geekshop.model.validation;

import geekshop.model.User;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Date;

/**
 *
 */
public class DateOfBirthValidator implements ConstraintValidator<DateOfBirth, String> {

    public void initialize(DateOfBirth dateOfBirth) {
    }

    public boolean isValid(String date, ConstraintValidatorContext constraintContext) {

        if (date == null || date.isEmpty())
            return true;

        Date dateOfBirth = User.strToDate(date.trim());
        return dateOfBirth != null && dateOfBirth.before(new Date());
    }

}
