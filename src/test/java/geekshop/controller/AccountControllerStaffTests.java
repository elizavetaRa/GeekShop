package geekshop.controller;

import geekshop.AbstractWebIntegrationTests;
import geekshop.model.*;
import org.junit.Before;
import org.junit.Test;
import org.salespointframework.useraccount.AuthenticationManager;
import org.salespointframework.useraccount.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class AccountControllerStaffTests extends AbstractWebIntegrationTests {

    @Autowired
    private AccountController controller;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PasswordRulesRepository passRulesRepo;
    @Autowired
    private MessageRepository messageRepo;
    @Autowired
    private AuthenticationManager authManager;

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
    @SuppressWarnings("unchecked")
    public void testStaff() {
        assertEquals("staff", controller.staff(model));
        List<User> employees = new LinkedList<User>();
        for (User user : userRepo.findAll()) {
            if (!user.getUserAccount().hasRole(new Role("ROLE_OWNER")) && user.getUserAccount().isEnabled()) {
                employees.add(user);
            }
        }
        assertThat((List<User>) model.asMap().get("staff"), containsInAnyOrder(employees.toArray(new User[employees.size()])));
    }

    @Test
    public void testShowEmployee() {
        assertEquals("profile", controller.showEmployee(model, employee.getUserAccount().getId()));
        assertEquals(employee, model.asMap().get("user"));
        assertFalse((boolean) model.asMap().get("isOwnProfile"));
        assertFalse((boolean) model.asMap().get("inEditingMode"));
    }

    @Test
    public void testHireGET() {
        assertEquals("profile", controller.hire(model));
        assertTrue((boolean) model.asMap().get("inEditingMode"));
    }

    @Test
    public void testHirePOST() {
        Map<String, String> formData = new HashMap<String, String>();
        formData.put("username", "test");
        formData.put("firstname", "Test");
        formData.put("lastname", "User");
        formData.put("email", "user@test.test");
        formData.put("gender", "SOMETHING_ELSE");
        formData.put("dateOfBirth", "12.12.1912");
        formData.put("maritalStatus", "UNKNOWN");
        formData.put("phone", "123");
        formData.put("street", "str");
        formData.put("houseNr", "123");
        formData.put("postcode", "12345");
        formData.put("place", "test");

        messageRepo.delete(messageRepo.findByMessageKind(MessageKind.NOTIFICATION));

//        assertEquals("redirect:/staff", controller.hire(model, formData));

        employee = null;
        for (User u : userRepo.findAll()) {
            if (u.getUserAccount().getFirstname().equals("Test") && u.getUserAccount().getLastname().equals("User"))
                employee = u;
        }
        assertNotNull(employee);

        assertEquals("username", "test", employee.getUserAccount().getUsername());
        assertEquals("firstname", "Test", employee.getUserAccount().getFirstname());
        assertEquals("lastname", "User", employee.getUserAccount().getLastname());
        assertEquals("email", "user@test.test", employee.getUserAccount().getEmail());
        assertEquals("gender", "SOMETHING_ELSE", employee.getGender().toString());
        assertEquals("dateOfBirth", User.strToDate("12.12.1912"), employee.getDateOfBirth());
        assertEquals("maritalStatus", "UNKNOWN", employee.getMaritalStatus().toString());
        assertEquals("phone", "123", employee.getPhone());
        assertEquals("street", "str", employee.getStreet());
        assertEquals("houseNr", "123", employee.getHouseNr());
        assertEquals("postcode", "12345", employee.getPostcode());
        assertEquals("place", "test", employee.getPlace());
        assertTrue(employee.getUserAccount().hasRole(new Role("ROLE_EMPLOYEE")));

        assertFalse(passwordRules.isValidPassword(employee.getPasswordAttributes()));

        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), not(is(emptyIterable())));
    }

    @Test
    public void testFire() {
        assertEquals("redirect:/staff", controller.fire(owner.getUserAccount().getId()));
        assertTrue(owner.getUserAccount().isEnabled());

        assertEquals("redirect:/staff", controller.fire(employee.getUserAccount().getId()));
        assertFalse(employee.getUserAccount().hasRole(new Role("ROLE_EMPLOYEE")));
        assertFalse(employee.getUserAccount().isEnabled());
    }

    @Test
    public void testFireAll() {
        assertEquals("redirect:/staff", controller.fireAll());
        for (User u : userRepo.findAll()) {
            if (!u.getUserAccount().hasRole(new Role("ROLE_OWNER"))) {
                assertFalse(u.getUserAccount().hasRole(new Role("ROLE_EMPLOYEE")));
                assertFalse(u.getUserAccount().isEnabled());
            }
        }
    }

    @Test
    public void testProfileChange() {
        assertEquals("profile", controller.profileChange(model, employee.getUserAccount().getId(), "changedata"));
        assertEquals(employee, model.asMap().get("user"));
        assertFalse((boolean) model.asMap().get("isOwnProfile"));
        assertTrue((boolean) model.asMap().get("inEditingMode"));

        assertEquals("changepw", controller.profileChange(model, employee.getUserAccount().getId(), "changepw"));
        assertEquals(employee, model.asMap().get("user"));
        assertFalse((boolean) model.asMap().get("isOwnProfile"));
        assertEquals(passwordRules, model.asMap().get("passwordRules"));
    }

}