package geekshop.model;

import geekshop.AbstractIntegrationTests;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Test class for {@link User}.
 *
 * @author Sebastian Döring
 */

public class UserTests extends AbstractIntegrationTests {

    @Autowired
    private UserAccountManager uam;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private JokeRepository jokeRepo;

    private UserAccount ua;
    private User user;
    private Joke joke0;
    private Joke joke1;
    private Joke joke2;
    private Joke joke3;
    private Joke joke4;
    private Joke joke5;

    @Before
    public void setUp() {
        ua = uam.create("user", "123");
        user = new User(ua, "aDVD!!1!", Gender.SOMETHING_ELSE, Date.from(Instant.now()), MaritalStatus.UNKNOWN, "123", "Qwertstr", "1a", "01234", "somewhere");
        Iterator<Joke> iterator = jokeRepo.findAll().iterator();
        joke0 = iterator.next();
        joke1 = iterator.next();
        joke2 = iterator.next();
        joke3 = iterator.next();
        joke4 = iterator.next();
        joke5 = iterator.next();
    }

    @Test
    public void testConstructor() {
        PasswordAttributes pa = user.getPasswordAttributes();
        Assert.assertTrue("HasUpperAndLower wrongly stored in PasswordAttributes!", pa.hasUpperAndLower());
        Assert.assertTrue("HasDigits wrongly stored in PasswordAttributes!", pa.hasDigits());
        Assert.assertTrue("HasSpecialCharacters wrongly stored in PasswordAttributes!", pa.hasSpecialCharacters());
        Assert.assertEquals("Wrong length stored in PasswordAttributes!", 8, pa.getLength());

        User user = new User(ua, "advd!1", Gender.SOMETHING_ELSE, Date.from(Instant.now()), MaritalStatus.UNKNOWN, "123", "Qwertstr", "1a", "01234", "somewhere");
        pa = user.getPasswordAttributes();
        Assert.assertFalse("HasUpperAndLower wrongly stored in PasswordAttributes!", pa.hasUpperAndLower());
        Assert.assertTrue("HasDigits wrongly stored in PasswordAttributes!", pa.hasDigits());
        Assert.assertTrue("HasSpecialCharacters wrongly stored in PasswordAttributes!", pa.hasSpecialCharacters());
        Assert.assertEquals("Wrong length stored in PasswordAttributes!", 6, pa.getLength());

        user = new User(ua, "aDVD!!!", Gender.SOMETHING_ELSE, Date.from(Instant.now()), MaritalStatus.UNKNOWN, "123", "Qwertstr", "1a", "01234", "somewhere");
        pa = user.getPasswordAttributes();
        Assert.assertTrue("HasUpperAndLower wrongly stored in PasswordAttributes!", pa.hasUpperAndLower());
        Assert.assertFalse("HasDigits wrongly stored in PasswordAttributes!", pa.hasDigits());
        Assert.assertTrue("HasSpecialCharacters wrongly stored in PasswordAttributes!", pa.hasSpecialCharacters());
        Assert.assertEquals("Wrong length stored in PasswordAttributes!", 7, pa.getLength());

        user = new User(ua, "aDVD1", Gender.SOMETHING_ELSE, Date.from(Instant.now()), MaritalStatus.UNKNOWN, "123", "Qwertstr", "1a", "01234", "somewhere");
        pa = user.getPasswordAttributes();
        Assert.assertTrue("HasUpperAndLower wrongly stored in PasswordAttributes!", pa.hasUpperAndLower());
        Assert.assertTrue("HasDigits wrongly stored in PasswordAttributes!", pa.hasDigits());
        Assert.assertFalse("HasSpecialCharacters wrongly stored in PasswordAttributes!", pa.hasSpecialCharacters());
        Assert.assertEquals("Wrong length stored in PasswordAttributes!", 5, pa.getLength());
    }

    @Test
    public void testAddJoke() {
        final String message = "addJoke seems to do its job improperly!";
        user.addJoke(joke0);
        List<Joke> recentJokes = user.getRecentJokes();
        Assert.assertTrue(message, recentJokes.size() == 1 && recentJokes.contains(joke0));
        user.addJoke(joke0);
        Assert.assertTrue(message, recentJokes.size() == 1 && recentJokes.contains(joke0));
        user.addJoke(joke1);
        Assert.assertTrue(message, recentJokes.size() == 2 && recentJokes.get(0).equals(joke0) && recentJokes.get(1).equals(joke1));
        user.addJoke(joke0);
        Assert.assertTrue(message, recentJokes.size() == 2 && recentJokes.get(0).equals(joke1) && recentJokes.get(1).equals(joke0));
        user.addJoke(joke2);
        user.addJoke(joke3);
        user.addJoke(joke4);
        user.addJoke(joke5);
        Assert.assertTrue(message, recentJokes.size() == 5 && recentJokes.get(0).equals(joke0) && recentJokes.get(1).equals(joke2)
                && recentJokes.get(2).equals(joke3) && recentJokes.get(3).equals(joke4) && recentJokes.get(4).equals(joke5));
    }

    @Test
    public void testGetLastJoke() {
        final String message = "getLastJoke seems to do its job improperly!";
        Assert.assertNull(message, user.getLastJoke());
        user.addJoke(joke0);
        Assert.assertEquals(message, joke0, user.getLastJoke());
        user.addJoke(joke1);
        Assert.assertEquals(message, joke1, user.getLastJoke());
        user.addJoke(joke0);
        Assert.assertEquals(message, joke0, user.getLastJoke());
        user.addJoke(joke2);
        Assert.assertEquals(message, joke2, user.getLastJoke());
        user.addJoke(joke3);
        Assert.assertEquals(message, joke3, user.getLastJoke());
        user.addJoke(joke4);
        Assert.assertEquals(message, joke4, user.getLastJoke());
        user.addJoke(joke5);
        Assert.assertEquals(message, joke5, user.getLastJoke());
    }
}
