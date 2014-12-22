package geekshop.controller;

import geekshop.GeekShop;
import geekshop.model.GSProduct;
import geekshop.model.GSProductOrders;
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
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import javax.transaction.Transactional;
import java.io.File;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = GeekShop.class)
@Transactional
public class OwnerControllerOrdersTest {

    @Autowired
    OwnerController controller;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    FilterChainProxy securityFilterChain;


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

        controller.orders(model, "sort=products");
        assertTrue(model.containsAttribute("orders"));

    }

    @Test
    public void createXML() throws Exception {
        login("owner", "123");

        File file = new File("orders.xml");

        controller.exportXML("products");

        assertTrue(file.exists());

    }

}