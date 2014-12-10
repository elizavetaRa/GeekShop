package geekshop;

import geekshop.model.Message;
import geekshop.model.MessageKind;
import geekshop.model.MessageRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Integration tests for {@link geekshop.model.MessageRepository}.
 * <p/>
 * *
 *
 * @author Felix D&ouml;ring
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = GeekShop.class)
@Transactional
public class MessageRepositoryTest {

    @Autowired
    MessageRepository messageRepo;

    @Test
    public void createAndDeleteEnty() {
        Message testNotification = new Message(MessageKind.NOTIFICATION, "Test");
        Message testReclaim = new Message(MessageKind.RECLAIM, "Test", "Reclaimlink");
        messageRepo.save(testNotification);
        messageRepo.save(testReclaim);

        assertThat(messageRepo.findAll(), hasItem(testNotification));
        assertThat(messageRepo.findAll(), hasItem(testReclaim));


        Long notiId = testNotification.getId();
        Long reclId = testReclaim.getId();
        messageRepo.delete(notiId);
        messageRepo.delete(reclId);

        assertThat(messageRepo.findAll(), not(hasItem(testNotification)));
        assertThat(messageRepo.findAll(), not(hasItem(testReclaim)));

    }


}
