package geekshop.controller;

import geekshop.AbstractWebIntegrationTests;
import geekshop.model.*;
import geekshop.model.validation.PersonalDataForm;
import org.junit.Before;
import org.junit.Test;
import org.salespointframework.useraccount.*;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.*;

public class AccountControllerStaffTests extends AbstractWebIntegrationTests {

    @Autowired
    private AccountController controller;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private UserAccountManager uam;
    @Autowired
    private PasswordRulesRepository passRulesRepo;
    @Autowired
    private MessageRepository messageRepo;
    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private Validator validator;

    private Model model;
    private User owner;
    private User employee;
    private PasswordRules passwordRules;

    private PersonalDataForm pdf;
    private WebDataBinder binder;


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

        pdf = new PersonalDataForm();
        binder = new WebDataBinder(pdf);
        binder.setValidator(validator); // use the validator from the context
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
        assertNotNull(model.asMap().get("personalDataForm"));
        assertFalse((boolean) model.asMap().get("isOwnProfile"));
        assertFalse((boolean) model.asMap().get("inEditingMode"));
    }

    @Test
    public void testHireGET() {
        assertEquals("profile", controller.hire(model));
        assertTrue((boolean) model.asMap().get("inEditingMode"));
        assertNotNull(model.asMap().get("personalDataForm"));
    }

    @Test
    public void testHirePOSTExisitingUsername() {
        UserAccount test = uam.create("test", "test");
        uam.save(test);

        Map<String, String> formData = new HashMap<String, String>();
        formData.put("username", "test");
        formData.put("firstname", "Test");
        formData.put("lastname", "User");
        formData.put("email", "user@test.test");
        formData.put("gender", "SOMETHING_ELSE");
        formData.put("dateOfBirth", "12.12.1912");
        formData.put("maritalStatus", "UNKNOWN");
        formData.put("phone", "(0351) 123456");
        formData.put("street", "str");
        formData.put("houseNr", "123");
        formData.put("postcode", "12345");
        formData.put("place", "test");

        messageRepo.delete(messageRepo.findByMessageKind(MessageKind.NOTIFICATION));

        MockHttpServletRequest  request = new MockHttpServletRequest("POST", "/addemployee");
        request.addParameters(formData); // populate the request
        binder.bind(new MutablePropertyValues(request.getParameterMap())); // triggering validation
        binder.getValidator().validate(binder.getTarget(), binder.getBindingResult());

        assertEquals("profile", controller.hire(model, (PersonalDataForm) binder.getTarget(), binder.getBindingResult()));

        assertTrue(binder.getBindingResult().hasFieldErrors("username"));
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), is(emptyIterable()));
    }

    @Test
    public void testHirePOSTValidUsername() {
        Map<String, String> formData = new HashMap<String, String>();
        formData.put("username", "test");
        formData.put("firstname", "Test");
        formData.put("lastname", "User");
        formData.put("email", "user@test.test");
        formData.put("gender", "SOMETHING_ELSE");
        formData.put("dateOfBirth", "12.12.1912");
        formData.put("maritalStatus", "UNKNOWN");
        formData.put("phone", "(0351) 123456");
        formData.put("street", "str");
        formData.put("houseNr", "123");
        formData.put("postcode", "12345");
        formData.put("place", "test");

        messageRepo.delete(messageRepo.findByMessageKind(MessageKind.NOTIFICATION));

        MockHttpServletRequest  request = new MockHttpServletRequest("POST", "/addemployee");
        request.addParameters(formData); // populate the request
        binder.bind(new MutablePropertyValues(request.getParameterMap())); // triggering validation
        binder.getValidator().validate(binder.getTarget(), binder.getBindingResult());

        assertEquals("redirect:/staff/test", controller.hire(model, (PersonalDataForm) binder.getTarget(), binder.getBindingResult()));

        assertFalse(binder.getBindingResult().hasErrors());

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
        assertEquals("phone", "(0351) 123456", employee.getPhone());
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
        assertFalse((boolean) model.asMap().get("isOwnProfile"));
        assertTrue((boolean) model.asMap().get("inEditingMode"));

        assertEquals("changepw", controller.profileChange(model, employee.getUserAccount().getId(), "changepw"));
        assertFalse((boolean) model.asMap().get("isOwnProfile"));
        assertEquals(passwordRules, model.asMap().get("passwordRules"));
    }

    @Test
    public void testChangedData() {
        Map<String, String> formData = new HashMap<String, String>();
        String uai = employee.getUserAccount().getId().toString();
        formData.put("uai", uai);
        formData.put("firstname", "Test");
        formData.put("lastname", "User");
        formData.put("username", employee.getUserAccount().getUsername());
        formData.put("email", "user@test.test");
        formData.put("gender", "SOMETHING_ELSE");
        formData.put("dateOfBirth", "12.12.1912");
        formData.put("maritalStatus", "UNKNOWN");
        formData.put("phone", "(0351) 123456");
        formData.put("street", "str");
        formData.put("houseNr", "123");
        formData.put("postcode", "12345");
        formData.put("place", "test");

        messageRepo.delete(messageRepo.findByMessageKind(MessageKind.NOTIFICATION));

        MockHttpServletRequest  request = new MockHttpServletRequest("POST", "/staff/" + uai + "/changedata");
        request.addParameters(formData); // populate the request
        binder.bind(new MutablePropertyValues(request.getParameterMap())); // triggering validation
        binder.getValidator().validate(binder.getTarget(), binder.getBindingResult());

        assertEquals("redirect:/staff/" + uai, controller.changedData(model, employee.getUserAccount().getId(), (PersonalDataForm)binder.getTarget(), binder.getBindingResult()));

        assertEquals("firstname", "Test", employee.getUserAccount().getFirstname());
        assertEquals("lastname", "User", employee.getUserAccount().getLastname());
        assertEquals("email", "user@test.test", employee.getUserAccount().getEmail());
        assertEquals("gender", "SOMETHING_ELSE", employee.getGender().toString());
        assertEquals("dateOfBirth", User.strToDate("12.12.1912"), employee.getDateOfBirth());
        assertEquals("maritalStatus", "UNKNOWN", employee.getMaritalStatus().toString());
        assertEquals("phone", "(0351) 123456", employee.getPhone());
        assertEquals("street", "str", employee.getStreet());
        assertEquals("houseNr", "123", employee.getHouseNr());
        assertEquals("postcode", "12345", employee.getPostcode());
        assertEquals("place", "test", employee.getPlace());

        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), is(emptyIterable()));
    }

    @Test
    public void testChangedPW() {
        messageRepo.deleteAll();
        assertEquals("changepw", controller.changedPW(model, employee.getUserAccount().getId(), " ", " "));
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), is(emptyIterable()));
        assertEquals("changepw", controller.changedPW(model, employee.getUserAccount().getId(), "!A2s3d4f", "!A2s3d4f5"));
        assertFalse(authManager.matches(new Password("!A2s3d4f"), employee.getUserAccount().getPassword()));
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), is(emptyIterable()));
        assertEquals("changepw", controller.changedPW(model, employee.getUserAccount().getId(), "1234", "1234"));
        assertFalse(authManager.matches(new Password("1234"), employee.getUserAccount().getPassword()));
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), is(emptyIterable()));
        assertNotEquals("changepw", controller.changedPW(model, employee.getUserAccount().getId(), "!A2s3d4f", "!A2s3d4f"));
        assertTrue(authManager.matches(new Password("!A2s3d4f"), employee.getUserAccount().getPassword()));
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), not(is(emptyIterable())));
    }
}