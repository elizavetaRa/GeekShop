package geekshop.controller;

import geekshop.AbstractWebIntegrationTests;
import geekshop.model.PasswordRules;
import geekshop.model.PasswordRulesRepository;
import geekshop.model.validation.SetRulesForm;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AccountControllerPasswordRulesTests extends AbstractWebIntegrationTests {

    @Autowired
    private AccountController controller;
    @Autowired
    private PasswordRulesRepository passRulesRepo;
    @Autowired
    private Validator validator;

    private Model model;
    private PasswordRules passwordRules;


    @Before
    public void setUp() {
        super.setUp();

        model = new ExtendedModelMap();

        login("owner", "123");

        passwordRules = passRulesRepo.findOne("passwordRules").get();
    }


    @Test
    public void testSetPWRulesGET() {
        assertEquals("setrules", controller.setPWRules(model));
        assertEquals(passwordRules, model.asMap().get("passwordRules"));
    }

    @Test
    public void testSetPWRulesPOST() {
        // minLength less than 6 charaters
        Map<String, String> formData = new HashMap<String, String>();
        formData.put("minLength", "4");
        formData.put("upperLower", "false");
        formData.put("digits", "");
        formData.put("specialChars", "true");

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/setrules");
        request.addParameters(formData); // populate the request
        SetRulesForm setRulesForm = new SetRulesForm();
        WebDataBinder binder = new WebDataBinder(setRulesForm);
        binder.setValidator(validator); // use the validator from the context
        binder.bind(new MutablePropertyValues(request.getParameterMap())); // triggering validation
        binder.getValidator().validate(binder.getTarget(), binder.getBindingResult());

        assertEquals("setrules", controller.setPWRules(model, formData, (SetRulesForm) binder.getTarget(), binder.getBindingResult()));

        assertTrue(binder.getBindingResult().hasErrors());

        // minLength valid
        formData.put("minLength", "6");
        request = new MockHttpServletRequest("POST", "/setrules");
        request.addParameters(formData);
        setRulesForm = new SetRulesForm();
        binder = new WebDataBinder(setRulesForm);
        binder.setValidator(validator); // use the validator from the context
        binder.bind(new MutablePropertyValues(request.getParameterMap()));
        binder.getValidator().validate(binder.getTarget(), binder.getBindingResult());

        assertEquals("redirect:/staff", controller.setPWRules(model, formData, (SetRulesForm) binder.getTarget(), binder.getBindingResult()));

        assertFalse(binder.getBindingResult().hasErrors());

        assertFalse(passwordRules.areUpperAndLowerNecessary());
        assertFalse(passwordRules.areDigitsNecessary());
        assertTrue(passwordRules.areSpecialCharactersNecessary());
        assertEquals(passwordRules.getMinLength(), 6);
    }

}