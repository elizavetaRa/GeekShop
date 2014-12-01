package geekshop.model;

/*
 * Created by Basti on 01.12.2014.
 */

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
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
    @GeneratedValue
    private Long id;

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
        return isLongEnough(password) && !password.matches(".*\\s.*") &&
                (!this.specialCharactersNecessary || containsSpecialCharacters(password)) &&
                (!this.upperAndLowerNecessary || containsUpperAndLower(password)) &&
                (!this.digitsNecessary || containsDigits(password));
    }

    private boolean containsSpecialCharacters(String password) {
        return password.matches(".*[^A-Za-z0-9\\s].*");
    }

    private boolean containsUpperAndLower(String password) {
        return password.matches(".*[A-Za-z].*");
    }

    private boolean containsDigits(String password) {
        return password.matches(".*[0-9].*");
    }

    private boolean isLongEnough(String password) {
        return password.length() >= this.minLength;
    }


    public boolean areSpecialCharactersNecessary() {
        return specialCharactersNecessary;
    }

    public void setSpecialCharactersNecessary(boolean specialCharactersNecessary) {
        this.specialCharactersNecessary = specialCharactersNecessary;
    }

    public boolean isUpperAndLowerNecessary() {
        return upperAndLowerNecessary;
    }

    public void setUpperAndLowerNecessary(boolean upperAndLowerNecessary) {
        this.upperAndLowerNecessary = upperAndLowerNecessary;
    }

    public boolean areNumbersNecessary() {
        return digitsNecessary;
    }

    public void setNumbersNecessary(boolean numbersNecessary) {
        this.digitsNecessary = numbersNecessary;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

}
