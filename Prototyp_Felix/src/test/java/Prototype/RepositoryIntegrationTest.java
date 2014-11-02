package Prototype;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Created by h4llow3En on 31/10/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@Transactional
public class RepositoryIntegrationTest {
    @Autowired
    ArticleRepository repository;

    @Test
    public void persistsRepositoryEntry() {

        Article entry = new Article("Article", 123.4f, "May the force be with you!", 45l);
        repository.save(entry);

        assertThat(repository.findAll(), hasItem(entry));
    }
}

