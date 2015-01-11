package geekshop.model;

import geekshop.AbstractIntegrationTests;
import org.junit.Assert;
import org.junit.Test;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Date;

/**
 * Test class for {@link UserRepository}.
 *
 * @author Sebastian Döring
 */

public class UserRepositoryTests extends AbstractIntegrationTests {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private UserAccountManager uam;


    @Test
    public void testFindByUserAccount() {
        UserAccount ua = uam.create("user", "123");
        uam.save(ua);
        User user = new User(ua, "123", Gender.SOMETHING_ELSE, Date.from(Instant.now()),
                MaritalStatus.UNKNOWN, "123", "str", "1a", "12345", "127.0.0.1");
        userRepo.save(user);
        Assert.assertEquals("UserRepository should find a user by user account!", user, userRepo.findByUserAccount(user.getUserAccount()));
    }
}
