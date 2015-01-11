package geekshop.controller;

import geekshop.AbstractWebIntegrationTests;
import geekshop.model.*;
import org.junit.Before;
import org.junit.Test;
import org.salespointframework.useraccount.AuthenticationManager;
import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import javax.servlet.http.HttpSession;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.*;

public class AccountControllerAuthenticationTests extends AbstractWebIntegrationTests {

    @Autowired
    private AccountController controller;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private MessageRepository messageRepo;
    @Autowired
    private JokeRepository jokeRepo;
    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private HttpSession session;

    private Model model;
    private User user;


    @Before
    public void setUp() {
        super.setUp();

        model = new ExtendedModelMap();

        login("owner", "123");
        user = userRepo.findByUserAccount(authManager.getCurrentUser().get());
    }


    @Test
    public void testIndexOwner() {
        String view = controller.index(model, Optional.of(user.getUserAccount()), session);

        assertEquals(user, session.getAttribute("user"));
        assertEquals(messageRepo, session.getAttribute("msgRepo"));
        assertTrue((boolean) session.getAttribute("isReclaim"));
        assertTrue((boolean) session.getAttribute("overview"));

        assertTrue(messageRepo.findByMessageKind(MessageKind.PASSWORD).iterator().hasNext());

        assertEquals("welcome", view);
        assertTrue(model.containsAttribute("joke"));

        // test whether password message to owner is sent only once, and whether a new joke is arranged only once a session
        Joke lastJoke = user.getLastJoke();
        controller.index(model, Optional.of(user.getUserAccount()), session);
        assertEquals(lastJoke, model.asMap().get("joke"));
        Iterator<Message> iterator = messageRepo.findByMessageKind(MessageKind.PASSWORD).iterator();
        assertTrue(iterator.next() != null);
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testIndexEmployee() {
        login("hans", "123");

        // Hans has an insecure password.
        User hans = userRepo.findByUserAccount(authManager.getCurrentUser().get());

        String view = controller.index(model, Optional.of(hans.getUserAccount()), session);

        assertEquals(hans, session.getAttribute("user"));
        assertEquals(messageRepo, session.getAttribute("msgRepo"));
        assertTrue((boolean) session.getAttribute("isReclaim"));
        assertTrue((boolean) session.getAttribute("overview"));

        assertEquals("adjustpw", view);
        assertThat(hans.getUserAccount().getRoles(), hasItem(new Role("ROLE_INSECURE_PASSWORD")));
        assertTrue(model.containsAttribute("passwordRules"));

        assertFalse(model.containsAttribute("joke"));

        // Hans adjustes his password.
        model = new ExtendedModelMap();

        controller.adjustPW(model, "!A2a$sA3", "!A2a$sA3", Optional.of(hans.getUserAccount()));

        view = controller.index(model, Optional.of(hans.getUserAccount()), session);

        assertThat(hans.getUserAccount().getRoles(), not(hasItem(new Role("ROLE_INSECURE_PASSWORD"))));

        assertEquals("welcome", view);
        assertTrue(model.containsAttribute("joke"));
    }

    @Test
    public void testGetRandomJoke() {
        jokeRepo.deleteAll();
        assertNull(controller.getRandomJoke(new LinkedList<Joke>()));

        Joke j1 = new Joke("joke1");
        Joke j2 = new Joke("joke2");
        Joke j3 = new Joke("joke3");
        Joke j4 = new Joke("joke4");
        Joke j5 = new Joke("joke5");
        Joke j6 = new Joke("joke6");
        jokeRepo.save(Arrays.asList(j1, j2, j3, j4, j5, j6));

        List<Joke> recent = Arrays.asList(j1, j2, j3);
        assertThat(controller.getRandomJoke(recent), anyOf(Arrays.asList(is(j4), is(j5), is(j6))));

        recent = Arrays.asList(j1, j2, j3, j4, j5);
        assertEquals(controller.getRandomJoke(recent), j6);

        recent = Arrays.asList(j2, j1, j3, j4, j5, j6);
        assertEquals(j2, controller.getRandomJoke(recent));
    }

    @Test
    public void adjustPW() {
        login("hans", "123");
        User hans = userRepo.findByUserAccount(authManager.getCurrentUser().get());
        messageRepo.deleteAll();
        assertEquals("adjustpw", controller.adjustPW(model, " ", " ", Optional.of(hans.getUserAccount())));
        assertTrue(model.containsAttribute("newPWError"));
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), is(emptyIterable()));
        assertEquals("adjustpw", controller.adjustPW(model, "!A2s3d4f", "!A2s3d4f5", Optional.of(hans.getUserAccount())));
        assertTrue(model.containsAttribute("retypePWError"));
        assertFalse(authManager.matches(new Password("!A2s3d4f"), hans.getUserAccount().getPassword()));
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), is(emptyIterable()));
        assertEquals("adjustpw", controller.adjustPW(model, "1234", "1234", Optional.of(hans.getUserAccount())));
        assertTrue(model.containsAttribute("newPWError"));
        assertFalse(authManager.matches(new Password("1234"), hans.getUserAccount().getPassword()));
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), is(emptyIterable()));
        assertEquals("redirect:/", controller.adjustPW(model, "!A2s3d4f", "!A2s3d4f", Optional.of(hans.getUserAccount())));
        assertTrue(authManager.matches(new Password("!A2s3d4f"), hans.getUserAccount().getPassword()));
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), not(is(emptyIterable())));
    }
}