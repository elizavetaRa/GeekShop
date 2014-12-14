package geekshop.model;

/*
 * Created by Basti on 14.12.2014.
 */

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


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
