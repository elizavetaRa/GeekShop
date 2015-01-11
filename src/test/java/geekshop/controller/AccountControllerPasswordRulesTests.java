package geekshop.controller;

import geekshop.AbstractWebIntegrationTests;
import geekshop.model.PasswordRules;
import geekshop.model.PasswordRulesRepository;
import geekshop.model.validation.SetRulesForm;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.*;

public class AccountControllerPasswordRulesTests extends AbstractWebIntegrationTests {

    @Autowired
    private AccountController controller;
    @Autowired
    private PasswordRulesRepository passRulesRepo;
    @Autowired
    private BindingResult result;

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
        Map<String, String> formData = new HashMap<String, String>();
        formData.put("minLength", "4");
        formData.put("upperLower", "false");
        formData.put("digits", null);
        formData.put("specialChars", "true");

        assertEquals("redirect:/staff", controller.setPWRules(model, formData, new SetRulesForm(), result));

        assertFalse(passwordRules.areUpperAndLowerNecessary());
        assertFalse(passwordRules.areDigitsNecessary());
        assertTrue(passwordRules.areSpecialCharactersNecessary());
        assertThat(passwordRules.getMinLength(), greaterThanOrEqualTo(4));
    }

}