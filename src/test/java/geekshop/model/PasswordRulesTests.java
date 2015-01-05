package geekshop.model;

import geekshop.AbstractIntegrationTests;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test class for {@link GSOrder}.
 *
 * @author Sebastian Döring
 */

public class PasswordRulesTests extends AbstractIntegrationTests {

    @Autowired
    private PasswordRulesRepository passRulesRepo;

    private PasswordRules passwordRules;
    private PasswordRules standardPWRules;

    @Before
    public void setUp() {
        passwordRules = passRulesRepo.findOne("passwordRules").get();
        standardPWRules = new PasswordRules();
    }

    @Test
    public void testGenerateRandomPassword() {
        String rndmPassword = passwordRules.generateRandomPassword();
        Assert.assertFalse("3 generated passwords should not be equal!", passwordRules.generateRandomPassword().equals(rndmPassword)
                && rndmPassword.equals(passwordRules.generateRandomPassword()));

        for (int i = 0; i < 10; i++) {
            Assert.assertTrue("A generated password should match the password rules!", passwordRules.isValidPassword(passwordRules.generateRandomPassword()));
        }
    }

    @Test
    public void testIsValidPassword() {
        Assert.assertFalse("Too short password should not be valid!", standardPWRules.isValidPassword("!A1a1a1"));
        Assert.assertFalse("Too short password should not be valid!",
                standardPWRules.isValidPassword(new PasswordAttributes(true, true, true, 7)));
        Assert.assertFalse("Passwords with white-space characters should not be valid!",
                standardPWRules.isValidPassword("!A1a1 a1"));
        Assert.assertFalse("Passwords without alphanumeric characters should not be valid!",
                standardPWRules.isValidPassword("!1\"2§3$4"));
        Assert.assertFalse("Passwords without upper- and lower-case characters should not be valid if such characters are necessary!",
                standardPWRules.isValidPassword("1a2s3d4$"));
        Assert.assertFalse("Passwords without upper- and lower-case characters should not be valid if such characters are necessary!",
                standardPWRules.isValidPassword(new PasswordAttributes(false, true, true, 8)));
        Assert.assertFalse("Passwords without digits should not be valid if digits are necessary!",
                standardPWRules.isValidPassword("!a\"s§d$F"));
        Assert.assertFalse("Passwords without digits should not be valid if digits are necessary!",
                standardPWRules.isValidPassword(new PasswordAttributes(true, false, true, 8)));
        Assert.assertFalse("Passwords without special characters should not be valid if special characters are necessary!",
                standardPWRules.isValidPassword("1A2s3D4f"));
        Assert.assertFalse("Passwords without special characters should not be valid if special characters are necessary!",
                standardPWRules.isValidPassword(new PasswordAttributes(true, true, false, 8)));
        Assert.assertTrue("Password Q\\6b*Z[} should be valid!", standardPWRules.isValidPassword("Q\\6b*Z[}"));
        Assert.assertTrue("Password Q\\6b*Z[} should be valid!",
                standardPWRules.isValidPassword(new PasswordAttributes(true, true, true, 8)));

        PasswordRules passwordRules2 = new PasswordRules(true, false, false, 4);
        Assert.assertFalse("Passwords without upper- and lower-case characters should not be valid if such characters are necessary!",
                passwordRules2.isValidPassword("abcd"));
        Assert.assertFalse("Passwords without upper- and lower-case characters should not be valid if such characters are necessary!",
                passwordRules2.isValidPassword(new PasswordAttributes(false, false, false, 4)));
        Assert.assertTrue("Password abCD should be valid!", passwordRules2.isValidPassword("abCD"));
        Assert.assertTrue("Password abCD should be valid!",
                passwordRules2.isValidPassword(new PasswordAttributes(true, false, false, 4)));

        PasswordRules passwordRules3 = new PasswordRules(false, true, false, 4);
        Assert.assertFalse("Passwords without digits should not be valid if digits are necessary!",
                passwordRules3.isValidPassword("!abc"));
        Assert.assertFalse("Passwords without digits should not be valid if digits are necessary!",
                passwordRules3.isValidPassword(new PasswordAttributes(false, false, true, 4)));
        Assert.assertTrue("Password 1abc should be valid!", passwordRules3.isValidPassword("1abc"));
        Assert.assertTrue("Password 1abc should be valid!",
                passwordRules3.isValidPassword(new PasswordAttributes(false, true, false, 4)));

        PasswordRules passwordRules4 = new PasswordRules(false, false, true, 4);
        Assert.assertFalse("Passwords without special characters should not be valid if special characters are necessary!",
                passwordRules4.isValidPassword("1A2s"));
        Assert.assertFalse("Passwords without special characters should not be valid if special characters are necessary!",
                passwordRules4.isValidPassword(new PasswordAttributes(true, true , false, 4)));
        Assert.assertTrue("Password abc! should be valid!", passwordRules4.isValidPassword("abc!"));
        Assert.assertTrue("Password abc! should be valid!",
                passwordRules4.isValidPassword(new PasswordAttributes(false, false, true, 4)));
    }

    @Test
    public void testContainsUpperAndLower() {
        Assert.assertFalse("Password abc does not contain upper- and lower-case letters!", PasswordRules.containsUpperAndLower("abc"));
        Assert.assertFalse("Password ABC does not contain upper- and lower-case letters!", PasswordRules.containsUpperAndLower("ABC"));
        Assert.assertTrue("Password AbC does contain upper- and lower-case letters!", PasswordRules.containsUpperAndLower("AbC"));
        Assert.assertTrue("Password bAc does contain upper- and lower-case letters!", PasswordRules.containsUpperAndLower("bAc"));
    }

    @Test
    public void testContainsDigits() {
        Assert.assertFalse("Password !SD does not contain digits!", PasswordRules.containsDigits("!SD"));
        Assert.assertTrue("Password 1SD does contain digits!", PasswordRules.containsDigits("1SD"));
        Assert.assertTrue("Password j4y does contain digits!", PasswordRules.containsDigits("j4y"));
    }

    @Test
    public void testContainsSpecialCharacters() {
        Assert.assertFalse("Password 1cd does not contain special characters!", PasswordRules.containsSpecialCharacters("1cd"));
        Assert.assertFalse("Password \"1 cd\" does not contain special characters!", PasswordRules.containsSpecialCharacters("1 cd"));
        Assert.assertTrue("Password !cd does contain special characters!", PasswordRules.containsSpecialCharacters("!cd"));
        Assert.assertTrue("Password 3d- does contain special characters!", PasswordRules.containsSpecialCharacters("3d-"));
        Assert.assertTrue("Password _ does contain special characters!", PasswordRules.containsSpecialCharacters("_"));
    }

    @Test
    public void testSetMinLength() {
        try {
            new PasswordRules(true, true, true, 3);
            Assert.fail("Passwords with less than 4 characters should not be allowed!");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            standardPWRules.setMinLength(3);
            Assert.fail("Passwords with less than 4 characters should not be allowed!");
        } catch (IllegalArgumentException ignored) {
        }
    }
}
