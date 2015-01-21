package geekshop.controller;

import geekshop.AbstractWebIntegrationTests;
import geekshop.model.*;
import org.junit.Before;
import org.junit.Test;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccountIdentifier;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by Midokin on 15.01.2015.
 */
public class AccountControllerRequestMappingTests extends AbstractWebIntegrationTests {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private UserAccountManager uam;

    private User hans;

    @Before
    public void setup() {
        hans = userRepo.findByUserAccount(uam.findByUsername("hans").get());

        login("owner", "123");

        super.setUp();
    }



    @Test
    public void accConIndexTest() throws Exception {
        mvc.perform(get("/index")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("joke"));
    }

    @Test
    public void accConAdjustPW() throws Exception {
        mvc.perform(get("/adjustpw")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        mvc.perform(post("/adjustpw")
                .with(user("hans").roles("EMPLOYEE"))
                .param("newPW", "!A2s3d4f")
                .param("retypePW", "!A2s3d4f"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void accConStaff() throws Exception {
        mvc.perform(get("/staff/")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("staff"))
                .andExpect(model().attributeExists("staff"));

        mvc.perform(get("/staff")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void accConShowEmployee() throws Exception {
        UserAccountIdentifier uai = hans.getUserAccount().getIdentifier();

        mvc.perform(get("/staff/" + uai.toString())
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("isOwnProfile"))
                .andExpect(model().attributeExists("inEditingMode"));

        mvc.perform(get("/staff/" + uai.toString())
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void accConHire() throws Exception {
        String newUserName = "hans1";

        mvc.perform(get("/addemployee")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("inEditingMode"))
                .andExpect(model().attributeExists("personalDataForm"));

        mvc.perform(post("/addemployee")
                .with(user("owner").roles("OWNER"))
                .param("firstname", "hans")
                .param("lastname", "hansen")
                .param("username", newUserName)
                .param("email", "hans@hansen.de")
                .param("gender", "MALE")
                .param("dateOfBirth", "01.01.1990")
                .param("maritalStatus", "UNMARRIED")
                .param("phone", "01231234567")
                .param("street", "hansstreet")
                .param("houseNr", "1")
                .param("postcode", "01234")
                .param("place", "hansstadt"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/staff/" + newUserName));

        mvc.perform(get("/addemployee")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());

        mvc.perform(post("/addemployee")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());

    }

    @Test
    public void accConFire() throws Exception {
        UserAccountIdentifier uai = hans.getUserAccount().getIdentifier();

        mvc.perform(delete("/staff/" + uai.toString())
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/staff"));

        mvc.perform(delete("/staff/" + uai.toString())
                .with(user("erna").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void accConFireAll() throws Exception {
        mvc.perform(delete("/firestaff")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/staff"));

        mvc.perform(delete("/firestaff")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void accConProfileChangeOwner() throws Exception {
        UserAccountIdentifier uai = hans.getUserAccount().getIdentifier();

        mvc.perform(get("/staff/" + uai.toString() + "/changedata")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("personalDataForm"))
                .andExpect(model().attributeExists("isOwnProfile"))
                .andExpect(model().attributeExists("inEditingMode"));

        mvc.perform(get("/staff/" + uai.toString() + "/changepw")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("changepw"))
                .andExpect(model().attributeExists("fullname"))
                .andExpect(model().attributeExists("isOwnProfile"))
                .andExpect(model().attributeExists("passwordRules"));

        mvc.perform(get("/staff/" + uai.toString() + "/changedata")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());

        mvc.perform(get("/staff/" + uai.toString() + "/changepw")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void accConChangedData() throws Exception {
        UserAccountIdentifier uai = hans.getUserAccount().getIdentifier();

        mvc.perform(post("/staff/" + uai.toString() + "/changedata")
                .with(user("owner").roles("OWNER"))
                .param("firstname", "hans")
                .param("lastname", "hansen")
                .param("username", uai.toString())
                .param("email", "hans@hansen.de")
                .param("gender", "MALE")
                .param("dateOfBirth", "01.01.1990")
                .param("maritalStatus", "UNMARRIED")
                .param("phone", "01231234567")
                .param("street", "hansstreet")
                .param("houseNr", "1")
                .param("postcode", "01234")
                .param("place", "hansstadt"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/staff/" + uai.toString()));

        mvc.perform(post("/staff/" + uai.toString() + "/changedata")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void accConChangedPW() throws Exception {
        UserAccountIdentifier uai = hans.getUserAccount().getIdentifier();

        mvc.perform(post("/staff/" + uai.toString() + "/changepw")
                .with(user("owner").roles("OWNER"))
                .param("newPW", "!A2s3d4f")
                .param("retypePW", "!A2s3d4f"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/staff/" + uai.toString()));

        mvc.perform(post("/staff/" + uai.toString() + "/changepw")
                .with(user("owner").roles("OWNER"))
                .param("newPW", "!A2s3d4f")
                .param("retypePW", "!A2s3d"))
                .andExpect(status().isOk())
                .andExpect(view().name("changepw"))
                .andExpect(model().attributeExists("fullname"))
                .andExpect(model().attributeExists("newPW"))
                .andExpect(model().attributeExists("retypePW"))
                .andExpect(model().attributeExists("isOwnProfile"))
                .andExpect(model().attributeExists("passwordRules"));

        mvc.perform(post("/staff/" + uai.toString() + "/changedata")
                .with(user("hans").roles("EMPLOYEE"))
                .param("newPW", "!A2s3d4f")
                .param("retypePW", "!A2s3d4f"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void accConSetPWRules() throws Exception {

        mvc.perform(get("/setrules")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("setrules"))
                .andExpect(model().attributeExists("passwordRules"))
                .andExpect(model().attributeExists("setRulesForm"));

        mvc.perform(get("/setrules")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());

        mvc.perform(post("/setrules")
                .with(user("owner").roles("OWNER"))
                .param("minLength", "8")
                .param("upperLower", "1")
                .param("digits", "1")
                .param("specialChars", "1"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/staff"));

        mvc.perform(post("/setrules")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void accConProfile() throws Exception {
        mvc.perform(get("/profile")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("isOwnProfile"))
                .andExpect(model().attributeExists("inEditingMode"));

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(get("/profile")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void accConProfileChange() throws Exception {
        mvc.perform(get("/profile/changedata")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("personalDataForm"))
                .andExpect(model().attributeExists("isOwnProfile"))
                .andExpect(model().attributeExists("inEditingMode"));

        mvc.perform(get("/profile/changepw")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(view().name("changepw"))
                .andExpect(model().attributeExists("fullname"))
                .andExpect(model().attributeExists("isOwnProfile"))
                .andExpect(model().attributeExists("passwordRules"));

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(get("/profile")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void accConChangedOwnData() throws Exception {
        mvc.perform(post("/profile/changedata")
                .with(user("hans").roles("EMPLOYEE"))
                .param("firstname", "hans")
                .param("lastname", "hansen")
                .param("username", "hans")
                .param("email", "hans@hansen.de")
                .param("gender", "MALE")
                .param("dateOfBirth", "01.01.1990")
                .param("maritalStatus", "UNMARRIED")
                .param("phone", "01231234567")
                .param("street", "hansstreet")
                .param("houseNr", "1")
                .param("postcode", "01234")
                .param("place", "hansstadt"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/profile"));

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(get("/profile")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void accConChangedOwnPW() throws Exception {
        String oldPW = "123";

        mvc.perform(post("/profile/changepw")
                .with(user("hans").roles("EMPLOYEE"))
                .param("oldPW", oldPW)
                .param("newPW", "!A2s3d4f")
                .param("retypePW", "!A2s3d4f"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/profile"));

        mvc.perform(post("/profile/changepw")
                .with(user("hans").roles("EMPLOYEE"))
                .param("oldPW", oldPW)
                .param("newPW", "!A2s3d4f")
                .param("retypePW", "!A2s3d"))
                .andExpect(status().isOk())
                .andExpect(view().name("changepw"))
                .andExpect(model().attributeExists("fullname"))
                .andExpect(model().attributeExists("oldPW"))
                .andExpect(model().attributeExists("newPW"))
                .andExpect(model().attributeExists("retypePW"))
                .andExpect(model().attributeExists("isOwnProfile"))
                .andExpect(model().attributeExists("passwordRules"));

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(post("/profile")
                .with(user("hans").roles("EMPLOYEE"))
                .param("oldPW", oldPW)
                .param("newPW", "!A2s3d4f")
                .param("retypePW", "!A2s3d4f"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }
}
