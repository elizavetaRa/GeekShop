package geekshop.controller;

import geekshop.AbstractWebIntegrationTests;
import geekshop.model.*;
import org.junit.Before;
import org.junit.Test;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by Midokin on 15.01.2015.
 */
public class CatalogControllerRequstMappingTests extends AbstractWebIntegrationTests {

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
    public void catConSearchEntryByName() throws Exception {
        mvc.perform(get("/productsearch")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(view().name("productsearch"))
                .andExpect(model().attributeExists("catalog"))
                .andExpect(model().attributeExists("superCategories"))
                .andExpect(model().attributeExists("subCategories"))
                .andExpect(model().attributeExists("inventory"));

        hans.getUserAccount().add(new Role("ROLE_INSECURE_PASSWORD"));
        userRepo.save(hans);

        mvc.perform(get("/productsearch")
                .with(user("hans").roles("EMPLOYEE")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }
}
