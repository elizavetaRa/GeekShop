package geekshop.model.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * The annotated String must represent a date before now in which day, month and year are formatted in the common order of the German-speaking area,
 * e. g. {@literal 14.03.95} or {@literal 14.03.1995}. Permitted as separators are white-space character, {@literal .}, {@literal -} and {@literal /}.
 */
@Target( {ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateOfBirthValidator.class)
@Documented
public @interface DateOfBirth {

    String message() default "Invalid date of birth";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
