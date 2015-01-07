package geekshop.model;

import geekshop.AbstractIntegrationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

/**
 * Test class for {@link JokeRepository}.
 *
 * @author Sebastian Döring
 */

public class JokeRepositoryTests extends AbstractIntegrationTests {

    @Autowired
    private JokeRepository jokeRepo;


    @Test
    public void testFindById() {
        Joke joke1 = new Joke("joke1");
        Joke joke2 = new Joke("joke2");
        Joke joke3 = new Joke("joke3");
        Joke joke4 = new Joke("joke4");
        jokeRepo.save(Arrays.asList(joke1, joke2, joke3));

        Assert.assertEquals("JokeRepository does not find correct joke by id!", joke1, jokeRepo.findById(joke1.getId()));
        Assert.assertEquals("JokeRepository does not find correct joke by id!", joke2, jokeRepo.findById(joke2.getId()));
        Assert.assertEquals("JokeRepository does not find correct joke by id!", joke3, jokeRepo.findById(joke3.getId()));
        Assert.assertNull("JokeRepository should not find any joke which was not stored!", jokeRepo.findById(joke4.getId()));
    }
}
