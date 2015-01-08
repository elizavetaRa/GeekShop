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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.*;

public class AccountControllerProfileTests extends AbstractWebIntegrationTests {

    @Autowired
    private AccountController controller;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PasswordRulesRepository passRulesRepo;
    @Autowired
    private MessageRepository messageRepo;
    @Autowired
    private JokeRepository jokeRepo;
    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private HttpSession session;

    private Model model;
    private User owner;
    private User employee;
    private PasswordRules passwordRules;


    @Before
    public void setUp() {
        super.setUp();

        model = new ExtendedModelMap();

        login("owner", "123");
        owner = userRepo.findByUserAccount(authManager.getCurrentUser().get());

        for (User u : userRepo.findAll()) {
            if (u.getUserAccount().hasRole(new Role("ROLE_EMPLOYEE")))
                employee = u;
        }


        passwordRules = passRulesRepo.findOne("passwordRules").get();
    }


    @Test
    public void testProfile() {
        assertEquals("profile", controller.profile(model, Optional.of(employee.getUserAccount())));
        assertEquals(model.asMap().get("user"), employee);
        assertTrue((boolean) model.asMap().get("isOwnProfile"));
        assertFalse((boolean) model.asMap().get("inEditingMode"));
    }

    @Test
    public void testProfileChange() {
        assertEquals("profile", controller.profileChange(model, "changedata", Optional.of(employee.getUserAccount())));
        assertEquals(employee, model.asMap().get("user"));
        assertTrue((boolean) model.asMap().get("isOwnProfile"));
        assertTrue((boolean) model.asMap().get("inEditingMode"));

        assertEquals("changepw", controller.profileChange(model, "changepw", Optional.of(employee.getUserAccount())));
        assertEquals(employee, model.asMap().get("user"));
        assertTrue((boolean) model.asMap().get("isOwnProfile"));
        assertEquals(passwordRules, model.asMap().get("passwordRules"));
    }

    @Test
    public void testChangedDataByOwner() { // owner changes personal data of employee
        Map<String, String> formData = new HashMap<String, String>();
        String uai = employee.getUserAccount().getId().toString();
        formData.put("uai", uai);
        formData.put("firstname", "Test");
        formData.put("lastname", "User");
        formData.put("email", "user@test.test");
        formData.put("gender", "SOMETHING_ELSE");
        formData.put("birthday", "12.12.1912");
        formData.put("maritalStatus", "UNKNOWN");
        formData.put("phone", "123");
        formData.put("street", "str");
        formData.put("houseNr", "123");
        formData.put("postcode", "12345");
        formData.put("place", "test");

        messageRepo.delete(messageRepo.findByMessageKind(MessageKind.NOTIFICATION));

        assertEquals("redirect:/staff/" + uai, controller.changedData(formData, Optional.of(owner.getUserAccount())));

        assertEquals("firstname", "Test", employee.getUserAccount().getFirstname());
        assertEquals("lastname", "User", employee.getUserAccount().getLastname());
        assertEquals("email", "user@test.test", employee.getUserAccount().getEmail());
        assertEquals("gender", "SOMETHING_ELSE", employee.getGender().toString());
        assertEquals("birthday", OwnerController.strToDate("12.12.1912"), employee.getBirthday());
        assertEquals("maritalStatus", "UNKNOWN", employee.getMaritalStatus().toString());
        assertEquals("phone", "123", employee.getPhone());
        assertEquals("street", "str", employee.getStreet());
        assertEquals("houseNr", "123", employee.getHouseNr());
        assertEquals("postcode", "12345", employee.getPostcode());
        assertEquals("place", "test", employee.getPlace());

        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), is(emptyIterable()));
    }

    @Test
    public void testChangedDataOwner() { // owner changes his own personal data
        Map<String, String> formData = new HashMap<String, String>();
        formData.put("uai", owner.getUserAccount().getId().toString());
        formData.put("firstname", "Test");
        formData.put("lastname", "User");
        formData.put("email", "user@test.test");
        formData.put("gender", "SOMETHING_ELSE");
        formData.put("birthday", "12.12.1912");
        formData.put("maritalStatus", "UNKNOWN");
        formData.put("phone", "123");
        formData.put("street", "str");
        formData.put("houseNr", "123");
        formData.put("postcode", "12345");
        formData.put("place", "test");

        messageRepo.delete(messageRepo.findByMessageKind(MessageKind.NOTIFICATION));

        assertEquals("redirect:/profile", controller.changedData(formData, Optional.of(owner.getUserAccount())));

        assertEquals("firstname", "Test", owner.getUserAccount().getFirstname());
        assertEquals("lastname", "User", owner.getUserAccount().getLastname());
        assertEquals("email", "user@test.test", owner.getUserAccount().getEmail());
        assertEquals("gender", "SOMETHING_ELSE", owner.getGender().toString());
        assertEquals("birthday", OwnerController.strToDate("12.12.1912"), owner.getBirthday());
        assertEquals("maritalStatus", "UNKNOWN", owner.getMaritalStatus().toString());
        assertEquals("phone", "123", owner.getPhone());
        assertEquals("street", "str", owner.getStreet());
        assertEquals("houseNr", "123", owner.getHouseNr());
        assertEquals("postcode", "12345", owner.getPostcode());
        assertEquals("place", "test", owner.getPlace());

        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), is(emptyIterable()));
    }

    @Test
    public void testChangedDataEmployee() { // employee himself changes personal data
        Map<String, String> formData = new HashMap<String, String>();
        formData.put("uai", employee.getUserAccount().getId().toString());
        formData.put("firstname", "Test");
        formData.put("lastname", "Employee");
        formData.put("email", "user@test.test");
        formData.put("gender", "SOMETHING_ELSE");
        formData.put("birthday", "12.12.1912");
        formData.put("maritalStatus", "UNKNOWN");
        formData.put("phone", "123");
        formData.put("street", "str");
        formData.put("houseNr", "123");
        formData.put("postcode", "12345");
        formData.put("place", "test");

        messageRepo.delete(messageRepo.findByMessageKind(MessageKind.NOTIFICATION));

        assertEquals("redirect:/profile", controller.changedData(formData, Optional.of(employee.getUserAccount())));

        assertEquals("firstname", "Test", employee.getUserAccount().getFirstname());
        assertEquals("lastname", "Employee", employee.getUserAccount().getLastname());
        assertEquals("email", "user@test.test", employee.getUserAccount().getEmail());
        assertEquals("gender", "SOMETHING_ELSE", employee.getGender().toString());
        assertEquals("birthday", OwnerController.strToDate("12.12.1912"), employee.getBirthday());
        assertEquals("maritalStatus", "UNKNOWN", employee.getMaritalStatus().toString());
        assertEquals("phone", "123", employee.getPhone());
        assertEquals("street", "str", employee.getStreet());
        assertEquals("houseNr", "123", employee.getHouseNr());
        assertEquals("postcode", "12345", employee.getPostcode());
        assertEquals("place", "test", employee.getPlace());

        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), not(is(emptyIterable())));
    }

    @Test
    public void testChangedOwnPW() {
        messageRepo.delete(messageRepo.findByMessageKind(MessageKind.NOTIFICATION));
        controller.changedOwnPW(model, " ", "!A2s3d4f", "!A2s3d4f", Optional.of(employee.getUserAccount()));
        assertFalse(authManager.matches(new Password("!A2s3d4f"), employee.getUserAccount().getPassword()));
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), is(emptyIterable()));
        controller.changedOwnPW(model, "12", "!A2s3d4f", "!A2s3d4f", Optional.of(employee.getUserAccount()));
        assertFalse(authManager.matches(new Password("!A2s3d4f"), employee.getUserAccount().getPassword()));
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), is(emptyIterable()));
        controller.changedOwnPW(model, "123", " ", " ", Optional.of(employee.getUserAccount()));
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), is(emptyIterable()));
        controller.changedOwnPW(model, "123", "!A2s3d4f", "!A2s3d4f5", Optional.of(employee.getUserAccount()));
        assertFalse(authManager.matches(new Password("!A2s3d4f"), employee.getUserAccount().getPassword()));
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), is(emptyIterable()));
        controller.changedOwnPW(model, "123", "1234", "1234", Optional.of(employee.getUserAccount()));
        assertFalse(authManager.matches(new Password("1234"), employee.getUserAccount().getPassword()));
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), is(emptyIterable()));
        controller.changedOwnPW(model, "123", "!A2s3d4f", "!A2s3d4f", Optional.of(employee.getUserAccount()));
        assertTrue(authManager.matches(new Password("!A2s3d4f"), employee.getUserAccount().getPassword()));
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), not(is(emptyIterable())));

        controller.index(model, Optional.of(owner.getUserAccount()), session);
        assertThat(messageRepo.findByMessageKind(MessageKind.PASSWORD), not(is(emptyIterable())));
        messageRepo.delete(messageRepo.findByMessageKind(MessageKind.NOTIFICATION));
        controller.changedOwnPW(model, "123", "!A2s3d4f", "!A2s3d4f", Optional.of(owner.getUserAccount()));
        assertTrue(authManager.matches(new Password("!A2s3d4f"), owner.getUserAccount().getPassword()));
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), is(emptyIterable()));
        assertThat(messageRepo.findByMessageKind(MessageKind.PASSWORD), is(emptyIterable()));
    }

    @Test
    public void testChangedPW() {
        messageRepo.deleteAll();
        controller.changedPW(model, " ", " ", employee.getUserAccount().getId());
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), is(emptyIterable()));
        controller.changedPW(model, "!A2s3d4f", "!A2s3d4f5", employee.getUserAccount().getId());
        assertFalse(authManager.matches(new Password("!A2s3d4f"), employee.getUserAccount().getPassword()));
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), is(emptyIterable()));
        controller.changedPW(model, "1234", "1234", employee.getUserAccount().getId());
        assertFalse(authManager.matches(new Password("1234"), employee.getUserAccount().getPassword()));
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), is(emptyIterable()));
        controller.changedPW(model, "!A2s3d4f", "!A2s3d4f", employee.getUserAccount().getId());
        assertTrue(authManager.matches(new Password("!A2s3d4f"), employee.getUserAccount().getPassword()));
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), not(is(emptyIterable())));
    }
}