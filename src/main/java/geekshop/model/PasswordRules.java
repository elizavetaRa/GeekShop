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

    private boolean specialCharactersNecessary;
    private boolean upperAndLowerNecessary;
    private boolean digitsNecessary;
    private int minLength;

    /**
     * Creates new {@link PasswordRules}. All options (special characters, upper and lower case, digits) are necessary.
     * Minimal length is set to 8.
     */
    public PasswordRules() {
        specialCharactersNecessary = true;
        upperAndLowerNecessary = true;
        digitsNecessary = true;
        minLength = 8;
    }

    /**
     * Creates new {@link PasswordRules} with the given flags and minimal length.
     */
    public PasswordRules(boolean specialCharactersNecessary, boolean upperAndLowerNecessary, boolean digitsNecessary, int minLength) {
        this.specialCharactersNecessary = specialCharactersNecessary;
        this.upperAndLowerNecessary = upperAndLowerNecessary;
        this.digitsNecessary = digitsNecessary;
        this.minLength = minLength;
    }


    /**
     * Generates a valid random password based on the rules set. Its length is equal to the minimal length set.
     */
    public String generateRandomPassword() {
        Random random = new SecureRandom();
        StringBuilder sb = new StringBuilder("abcdefghijklmnopqrstuvwxyz");
        if (this.specialCharactersNecessary)
            sb.append("!\"§$%&/=?`´\\()[]{}³²^°+#*'~,.-;:_<>|@€");
        if (this.upperAndLowerNecessary)
            sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        if (this.digitsNecessary)
            sb.append("0123456789");
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
     * @return {@code True} if and only if the set conditions are fulfilled, the password is long enough, does not contain any white space characters and contains at least one alphanumeric character.
     */
    public boolean isValidPassword(String password) {
        return isLongEnough(password) && !password.matches(".*\\s.*") && password.matches(".*\\w.*") &&
                (!this.specialCharactersNecessary || containsSpecialCharacters(password)) &&
                (!this.upperAndLowerNecessary || containsUpperAndLower(password)) &&
                (!this.digitsNecessary || containsDigits(password));
    }

    /**
     * Validates the given password in the form of {@link PasswordAttributes} according to the rules.
     * @return {@code True} if and only if the set conditions are fulfilled, the password is long enough, does not contain any white space characters and contains at least one alphanumeric character.
     */
    public boolean isValidPassword(PasswordAttributes passwordAttributes) {
        return isLongEnough(passwordAttributes.getLength()) &&
                (!this.specialCharactersNecessary || passwordAttributes.hasSpecialCharacters()) &&
                (!this.upperAndLowerNecessary || passwordAttributes.hasUpperAndLower()) &&
                (!this.digitsNecessary || passwordAttributes.hasDigits());
    }

    /**
     * Checks whether at least one special character, i. e. no Latin letter, digit or white space character, is contained in the given password.
     */
    public static boolean containsSpecialCharacters(String password) {
        return password.matches(".*[^A-Za-z0-9\\s].*");
    }

    /**
     * Checks whether at least one upper- and one lower-case characters are contained in the given password.
     */
    public static boolean containsUpperAndLower(String password) {
        return password.matches(".*[A-Za-z].*");
    }

    /**
     * Checks whether at least one digit is contained in the given password.
     */
    public static boolean containsDigits(String password) {
        return password.matches(".*[0-9].*");
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


    public boolean areSpecialCharactersNecessary() {
        return specialCharactersNecessary;
    }

    public void setSpecialCharactersNecessary(boolean specialCharactersNecessary) {
        this.specialCharactersNecessary = specialCharactersNecessary;
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

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }
}
