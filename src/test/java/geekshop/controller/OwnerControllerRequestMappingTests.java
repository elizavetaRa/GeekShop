package geekshop.controller;

import geekshop.AbstractWebIntegrationTests;
import geekshop.model.*;
import org.junit.Before;
import org.junit.Test;
import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.ProductIdentifier;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

/**
 * Created by Midokin on 15.01.2015.
 */
public class OwnerControllerRequestMappingTests extends AbstractWebIntegrationTests {

    @Autowired
    private Catalog<GSProduct> catalog;
    @Autowired
    private MessageRepository messageRepo;
    @Autowired
    private JokeRepository jokeRepo;

    @Before
    public void setup() {
        login("owner", "123");

        super.setUp();
    }



    @Test
    public void ownConOrders() throws Exception {
        mvc.perform(get("/orders")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("setOrders"))
                .andExpect(model().attributeExists("userRepo"))
                .andExpect(model().attributeExists("catalog"));
    }

    @Test
    public void ownConExportXML() throws Exception {
        mvc.perform(get("/exportxml")
                .with(user("owner").roles("OWNER"))
                .param("sort", "products"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/orders?sort=products"));

        mvc.perform(get("/exportxml")
                .with(user("owner").roles("OWNER"))
                .param("sort", "orders"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/orders?sort=orders"));
    }

    @Test
    public void ownConShowReclaim() throws Exception {
        Message message = messageRepo.findByMessageKind(MessageKind.RECLAIM).iterator().next();
        long messageID = message.getId();
        String reclaimID = message.getReclaimId();

        mvc.perform(post("/showreclaim/" + reclaimID)
                .with(user("owner").roles("OWNER"))
                .param("msgId", String.valueOf(messageID)))
                .andExpect(status().isOk())
                .andExpect(view().name("showreclaim"))
                .andExpect(model().attributeExists("rid"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("order"));

        mvc.perform(delete("/showreclaim/" + reclaimID)
                .with(user("owner").roles("OWNER"))
                .param("msgId", String.valueOf(messageID))
                .param("accept", String.valueOf(true)))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/messages"));
    }

    @Test
    public void ownConJokes() throws Exception {
        long id = jokeRepo.findAll().iterator().next().getId();

        mvc.perform(get("/jokes")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("jokes"))
                .andExpect(model().attributeExists("jokes"));

        mvc.perform(get("/jokes/" + String.valueOf(id))
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("editjoke"))
                .andExpect(model().attributeExists("joke"));

        mvc.perform(delete("/jokes/" + String.valueOf(id))
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/jokes"));
    }

    @Test
    public void ownConNewJoke() throws Exception {
        String jokeText = "testjoke";

        mvc.perform(get("/newjoke")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("editjoke"));

        mvc.perform(post("/newjoke")
                .with(user("owner").roles("OWNER"))
                .param("jokeText", jokeText))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/jokes"));
    }

    @Test
    public void ownConEditJoke() throws Exception {
        long id = jokeRepo.findAll().iterator().next().getId();
        String jokeText = "testjoke";

        mvc.perform(post("/editjoke/" + String.valueOf(id))
                .with(user("owner").roles("OWNER"))
                .param("jokeText", jokeText))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/jokes"));
    }

    @Test
    public void ownConDeleteAllJokes() throws Exception {
        mvc.perform(delete("/deljokes")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/jokes"));
    }

    @Test
    public void ownConMessages() throws Exception {
        long id = messageRepo.findAll().iterator().next().getId();

        mvc.perform(get("/messages")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("messages"))
                .andExpect(model().attributeExists("ownermessage"));

        mvc.perform(delete("/messages/" + String.valueOf(id))
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/messages"));
    }

    @Test
    public void ownConRange() throws Exception {
        mvc.perform(get("/range")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("range"))
                .andExpect(model().attributeExists("inventory"))
                .andExpect(model().attributeExists("supercategories"));
    }

    @Test
    public void ownConRangeDel() throws Exception {
        mvc.perform(delete("/range/delsuper")
                .with(user("owner").roles("OWNER"))
                .param("superName", "Kleidung"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));

        mvc.perform(delete("/range/delsub")
                .with(user("owner").roles("OWNER"))
                .param("subName", "Aufkleber"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));

        ProductIdentifier productID = catalog.findByName("USB-Staubsauger").iterator().next().getIdentifier();

        mvc.perform(delete("/range/delproduct")
                .with(user("owner").roles("OWNER"))
                .param("productIdent", productID.toString()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));
    }

    @Test
    public void ownConEditProduct() throws Exception {
        ProductIdentifier productID = catalog.findAll().iterator().next().getIdentifier();

        mvc.perform(get("/range/editproduct/" + productID.toString())
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("editproduct"))
                .andExpect(model().attributeExists("productForm"))
                .andExpect(model().attributeExists("productName"))
                .andExpect(model().attributeExists("superCategories"))
                .andExpect(model().attributeExists("isNew"));

        mvc.perform(post("/range/editproduct/" + productID.toString())
                .with(user("owner").roles("OWNER"))
                .param("name", "Täst")
                .param("productNumber", "1234567")
                .param("price", "1.234.567,89 €")
                .param("minQuantity", "5")
                .param("quantity", "10")
                .param("subCategory", "Informatik"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));
    }

    @Test
    public void ownConAddProduct() throws Exception {
        mvc.perform(get("/range/addproduct")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("editproduct"))
                .andExpect(model().attributeExists("productForm"))
                .andExpect(model().attributeExists("superCategories"))
                .andExpect(model().attributeExists("isNew"));

        mvc.perform(post("/range/addproduct")
                .with(user("owner").roles("OWNER"))
                .param("name", "Täst")
                .param("productNumber", "1234567")
                .param("price", "1.234.567,89 €")
                .param("minQuantity", "5")
                .param("quantity", "10")
                .param("subCategory", "Informatik"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));
    }

    @Test
    public void ownConEditSuperCategory() throws Exception {
        mvc.perform(get("/range/editsuper/Kleidung")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("editsuper"))
                .andExpect(model().attributeExists("super"))
                .andExpect(model().attributeExists("name"));

        mvc.perform(post("/range/editsuper/Kleidung")
                .with(user("owner").roles("OWNER"))
                .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("editsuper"))
                .andExpect(model().attributeExists("super"))
                .andExpect(model().attributeExists("name"))
                .andExpect(model().attributeExists("nameError"));

        mvc.perform(post("/range/editsuper/Kleidung")
                .with(user("owner").roles("OWNER"))
                .param("name", "Dekoration"))
                .andExpect(status().isOk())
                .andExpect(view().name("editsuper"))
                .andExpect(model().attributeExists("super"))
                .andExpect(model().attributeExists("name"))
                .andExpect(model().attributeExists("nameError"));

        mvc.perform(post("/range/editsuper/Kleidung")
                .with(user("owner").roles("OWNER"))
                .param("name", "newSuperName"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));
    }

    @Test
    public void ownConAddSuper() throws Exception {
        String name = "testName";

        mvc.perform(get("/range/addsuper")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("editsuper"));

        mvc.perform(post("/range/addsuper")
                .with(user("owner").roles("OWNER"))
                .param("name", name))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));

        mvc.perform(post("/range/addsuper")
                .with(user("owner").roles("OWNER"))
                .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("editsuper"))
                .andExpect(model().attributeExists("name"))
                .andExpect(model().attributeExists("nameError"));
    }

    @Test
    public void ownConEditSubCategory() throws Exception {
        String newSub = "newTestName";

        mvc.perform(get("/range/editsub/Informatik")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("editsub"))
                .andExpect(model().attributeExists("sub"))
                .andExpect(model().attributeExists("superCategory"))
                .andExpect(model().attributeExists("name"))
                .andExpect(model().attributeExists("superCategories"));

        mvc.perform(get("/range/editsub/notExisting")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));

        mvc.perform(post("/range/editsub/notExisting")
                .with(user("owner").roles("OWNER"))
                .param("name", newSub)
                .param("superCategory", "Kleidung"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));

        mvc.perform(post("/range/editsub/Informatik")
                .with(user("owner").roles("OWNER"))
                .param("name", "")
                .param("superCategory", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("editsub"))
                .andExpect(model().attributeExists("sub"))
                .andExpect(model().attributeExists("superCategory"))
                .andExpect(model().attributeExists("name"))
                .andExpect(model().attributeExists("superCategories"));

        mvc.perform(post("/range/editsub/Informatik")
                .with(user("owner").roles("OWNER"))
                .param("name", newSub)
                .param("superCategory", "Kleidung"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));
    }

    @Test
    public void ownConAddSubCategory() throws Exception {
        String name = "newSub";

        mvc.perform(get("/range/addsub")
                .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("editsub"))
                .andExpect(model().attributeExists("superCategories"));

        mvc.perform(post("/range/addsub")
                .with(user("owner").roles("OWNER"))
                .param("name", name)
                .param("superCategory", "Kleidung"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/range"));

        mvc.perform(post("/range/addsub")
                .with(user("owner").roles("OWNER"))
                .param("name", "")
                .param("superCategory", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("editsub"))
                .andExpect(model().attributeExists("superCategory"))
                .andExpect(model().attributeExists("name"))
                .andExpect(model().attributeExists("superCategories"))
                .andExpect(model().attributeExists("nameError"));
    }

}
