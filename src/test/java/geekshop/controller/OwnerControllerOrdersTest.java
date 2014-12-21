package geekshop.controller;

import geekshop.GeekShop;
import geekshop.model.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.context.WebApplicationContext;

import javax.transaction.Transactional;
import java.io.File;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = GeekShop.class)
@Transactional
public class OwnerControllerOrdersTest {

    @Autowired OwnerController controller;

    @Autowired AuthenticationManager authenticationManager;
    @Autowired WebApplicationContext context;
    @Autowired FilterChainProxy securityFilterChain;

    protected MockMvc mvc;

    @Before
    public void setUp() {

        context.getServletContext().setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, context);

        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(securityFilterChain)
                .build();
    }

    protected void login(String username, String password) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
        SecurityContextHolder.getContext().setAuthentication(authenticationManager.authenticate(authentication));
    }

    @Test
    public void mapNotNull() throws Exception {
        login("owner", "123");

        Map<GSProduct, GSProductOrders> map = controller.putMap();
        assertNotNull(map);

    }

    @Test
    public void mapExistsInModel() throws Exception {

        Model model = new ExtendedModelMap();

        login("owner", "123");

        controller.orders(model);
        assertTrue(model.containsAttribute("orders"));

    }

    @Test
    public void createXML() throws Exception {
        login("owner", "123");

        File file = new File("Sales.xml");

        controller.exportXML();

        assertTrue(file.exists());

    }

}