package geekshop.controller;

import geekshop.AbstractWebIntegrationTests;
import geekshop.model.GSProduct;
import geekshop.model.GSProductOrders;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.io.File;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class OwnerControllerOrdersTests extends AbstractWebIntegrationTests {

    @Autowired
    OwnerController controller;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    FilterChainProxy securityFilterChain;

    @Before
    public void setUp() {
        login("owner", "123");
    }

    @Test
    public void mapNotNull() throws Exception {

        Map<GSProduct, GSProductOrders> map = controller.putMap();
        assertNotNull(map);

    }

    @Test
    public void mapExistsInModel() throws Exception {

        Model model = new ExtendedModelMap();

        controller.orders(model, "orders");
        assertTrue(model.containsAttribute("setOrders"));

    }

    @Test
    public void createXML() throws Exception {

        File file = new File("orders.xml");

        controller.exportXML("products");

        assertTrue(file.exists());

    }

}