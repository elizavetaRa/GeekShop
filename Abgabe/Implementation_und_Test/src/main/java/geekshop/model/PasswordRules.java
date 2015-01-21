package geekshop.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.security.SecureRandom;
import java.util.Random;

/**
 * This class encapsulates the rules with which {@link geekshop.model.User}'s {@link org.salespointframework.useraccount.Password}s are validated.
 *
 * @author Sebastian Döring
 */

@Entity
public class PasswordRules {
    /**
     * Because there ought to exist only one instance of this class, the ID is always initialized with "{@code passwordRules}".
     */
    @Id
    private final String id = "passwordRules";

    private boolean upperAndLowerNecessary;
    private boolean digitsNecessary;
    private boolean specialCharactersNecessary;
    private int minLength;

    /**
     * Creates new {@link PasswordRules}. All options (special characters, upper and lower case, digits) are necessary.
     * Minimal length is set to 8.
     */
    public PasswordRules() {
        upperAndLowerNecessary = true;
        digitsNecessary = true;
        specialCharactersNecessary = true;
        minLength = 8;
    }

    /**
     * Creates new {@link PasswordRules} with the given flags and minimal length.
     *
     * @param minLength must be at least 4.
     * @throws IllegalArgumentException if {@literal minLength} is less than 4.
     */
    public PasswordRules(boolean upperAndLowerNecessary, boolean digitsNecessary, boolean specialCharactersNecessary, int minLength) {
        if (minLength < 4)
            throw new IllegalArgumentException("Minimal password length has to be at least 4 characters!");

        this.upperAndLowerNecessary = upperAndLowerNecessary;
        this.digitsNecessary = digitsNecessary;
        this.specialCharactersNecessary = specialCharactersNecessary;
        this.minLength = minLength;
    }


    /**
     * Generates a valid random password based on the rules set. Its length is equal to the minimal length set.
     */
    public String generateRandomPassword() {
        Random random = new SecureRandom();
        StringBuilder sb = new StringBuilder("abcdefghijklmnopqrstuvwxyz");
        if (this.upperAndLowerNecessary)
            sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        if (this.digitsNecessary)
            sb.append("0123456789");
        if (this.specialCharactersNecessary)
            sb.append("!\"§$%&/=?`´\\()[]{}³²^°+#*'~,.-;:_<>|@€");
        char[] charSet = sb.toString().toCharArray();

        char[] result;
        do {
            result = new char[this.minLength];

            for (int i = 0; i < result.length; i++) {
                // picks a random index out of character set
                int randomCharIndex = random.nextInt(charSet.length);
                result[i] = charSet[randomCharIndex];
            }
        } while (!isValidPassword(new String(result)));

        return new String(result);
    }

    /**
     * Validates the given password according to the rules.
     *
     * @return {@code True} if and only if the set conditions are fulfilled, the password is long enough, does not contain any white-space characters and contains at least one letter.
     */
    public boolean isValidPassword(String password) {
        return isLongEnough(password) && !password.matches(".*\\s.*") && password.matches(".*[a-zA-Z].*") &&
                (!this.upperAndLowerNecessary || containsUpperAndLower(password)) &&
                (!this.digitsNecessary || containsDigits(password)) &&
                (!this.specialCharactersNecessary || containsSpecialCharacters(password));
    }

    /**
     * Validates the given password in the form of {@link PasswordAttributes} according to the rules.
     *
     * @return {@code True} if and only if the set conditions are fulfilled, the password is long enough, does not contain any white-space characters and contains at least one letter.
     */
    public boolean isValidPassword(PasswordAttributes passwordAttributes) {
        return isLongEnough(passwordAttributes.getLength()) &&
                (!this.upperAndLowerNecessary || passwordAttributes.hasUpperAndLower()) &&
                (!this.digitsNecessary || passwordAttributes.hasDigits()) &&
                (!this.specialCharactersNecessary || passwordAttributes.hasSpecialCharacters());
    }

    /**
     * Checks whether at least one upper- and one lower-case character are contained in the given password.
     */
    public static boolean containsUpperAndLower(String password) {
        return password.matches(".*[a-z].*") && password.matches(".*[A-Z].*");
    }

    /**
     * Checks whether at least one digit is contained in the given password.
     */
    public static boolean containsDigits(String password) {
        return password.matches(".*[0-9].*");
    }

    /**
     * Checks whether at least one special character, i. e. all but Latin letter, digit and white-space character, is contained in the given password.
     */
    public static boolean containsSpecialCharacters(String password) {
        return password.matches(".*[^A-Za-z0-9\\s].*");
    }

    /**
     * Checks whether the given password's length is equal or more than the set minimal length.
     */
    private boolean isLongEnough(String password) {
        return password.length() >= this.minLength;
    }

    /**
     * Checks whether the given password length is equal or more than the set minimal length.
     */
    private boolean isLongEnough(int passwordLength) {
        return passwordLength >= this.minLength;
    }


    public boolean areUpperAndLowerNecessary() {
        return upperAndLowerNecessary;
    }

    public void setUpperAndLowerNecessary(boolean upperAndLowerNecessary) {
        this.upperAndLowerNecessary = upperAndLowerNecessary;
    }

    public boolean areDigitsNecessary() {
        return digitsNecessary;
    }

    public void setDigitsNecessary(boolean digitsNecessary) {
        this.digitsNecessary = digitsNecessary;
    }

    public boolean areSpecialCharactersNecessary() {
        return specialCharactersNecessary;
    }

    public void setSpecialCharactersNecessary(boolean specialCharactersNecessary) {
        this.specialCharactersNecessary = specialCharactersNecessary;
    }

    public int getMinLength() {
        return minLength;
    }

    /**
     * @param minLength must be at least 4.
     * @throws IllegalArgumentException if {@literal minLength} is less than 4.
     */
    public void setMinLength(int minLength) {
        if (minLength < 4)
            throw new IllegalArgumentException("Minimal password length has to be at least 4 characters!");

        this.minLength = minLength;
    }
}
