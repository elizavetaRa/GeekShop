package geekshop.model.validation;

import org.hibernate.validator.constraints.Range;

/**
 * Validation class for form setting password rules.
 *
 * @author Sebastian Döring
 */
public class SetRulesForm {

    @Range(min = 6L, message = "Die minimale Länge eines Passwortes muss mindestens 6 Zeichen betragen.")
    private String minLength;

    public String getMinLength() {
        return minLength;
    }

    public void setMinLength(String minLength) {
        this.minLength = minLength;
    }
}
