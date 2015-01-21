package geekshop.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Represents the attributes of an password to easily determine whether this password matches the password rules.
 *
 * @author Sebastian Döring
 */

@Entity
public class PasswordAttributes {
    @Id
    @GeneratedValue
    private Long id;

    private boolean hasUpperAndLower;
    private boolean hasDigits;
    private boolean hasSpecialCharacters;
    private int length;


    @Deprecated
    protected PasswordAttributes() {
    }

    /**
     * Creates new {@link PasswordAttributes} with the given flags and length.
     */
    public PasswordAttributes(boolean hasUpperAndLower, boolean hasDigits, boolean hasSpecialCharacters, int length) {
        this.hasUpperAndLower = hasUpperAndLower;
        this.hasDigits = hasDigits;
        this.hasSpecialCharacters = hasSpecialCharacters;
        this.length = length;
    }


    public boolean hasUpperAndLower() {
        return hasUpperAndLower;
    }

    public void setHasUpperAndLower(boolean hasUpperAndLower) {
        this.hasUpperAndLower = hasUpperAndLower;
    }

    public boolean hasDigits() {
        return hasDigits;
    }

    public void setHasDigits(boolean hasDigits) {
        this.hasDigits = hasDigits;
    }

    public boolean hasSpecialCharacters() {
        return hasSpecialCharacters;
    }

    public void setHasSpecialCharacters(boolean hasSpecialCharacters) {
        this.hasSpecialCharacters = hasSpecialCharacters;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
