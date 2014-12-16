package geekshop.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.security.SecureRandom;
import java.util.Random;

/**
 * This class encapsulates the rules with which {@link geekshop.model.User}'s {@link org.salespointframework.useraccount.Password}s are validated.
 *
 * @author Sebastian D&ouml;ring
 */

@Entity
public class PasswordRules {
    @Id
    private final String id = "passwordRules";

    private boolean specialCharactersNecessary;
    private boolean upperAndLowerNecessary;
    private boolean digitsNecessary;
    private int minLength;


    public PasswordRules() {
        specialCharactersNecessary = true;
        upperAndLowerNecessary = true;
        digitsNecessary = true;
        minLength = 8;
    }

    public PasswordRules(boolean specialCharactersNecessary, boolean upperAndLowerNecessary, boolean digitsNecessary, int minLength) {
        this.specialCharactersNecessary = specialCharactersNecessary;
        this.upperAndLowerNecessary = upperAndLowerNecessary;
        this.digitsNecessary = digitsNecessary;
        this.minLength = minLength;
    }


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

    public boolean isValidPassword(String password) {
        return isLongEnough(password) && !password.matches(".*\\s.*") && password.matches(".*\\w.*") &&
                (!this.specialCharactersNecessary || containsSpecialCharacters(password)) &&
                (!this.upperAndLowerNecessary || containsUpperAndLower(password)) &&
                (!this.digitsNecessary || containsDigits(password));
    }

    public boolean isValidPassword(PasswordAttributes passwordAttributes) {
        return isLongEnough(passwordAttributes.getLength()) &&
                (!this.specialCharactersNecessary || passwordAttributes.hasSpecialCharacters()) &&
                (!this.upperAndLowerNecessary || passwordAttributes.hasUpperAndLower()) &&
                (!this.digitsNecessary || passwordAttributes.hasDigits());
    }

    public static boolean containsSpecialCharacters(String password) {
        return password.matches(".*[^A-Za-z0-9\\s].*");
    }

    public static boolean containsUpperAndLower(String password) {
        return password.matches(".*[A-Za-z].*");
    }

    public static boolean containsDigits(String password) {
        return password.matches(".*[0-9].*");
    }

    private boolean isLongEnough(String password) {
        return password.length() >= this.minLength;
    }

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
