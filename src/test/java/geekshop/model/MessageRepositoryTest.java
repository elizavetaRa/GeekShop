package geekshop.model;

import geekshop.AbstractIntegrationTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Integration tests for {@link geekshop.model.MessageRepository}.
 *
 * @author Felix Döring
 */

public class MessageRepositoryTest extends AbstractIntegrationTests {

    @Autowired
    MessageRepository messageRepo;
    @Autowired
    UserRepository userRepo;
    @Autowired
    GSOrderRepository orderRepo;

    @Test
    public void createAndDeleteEnty() {
        GSOrder order = orderRepo.findByType(OrderType.RECLAIM).iterator().next();
        Message testNotification = new Message(MessageKind.NOTIFICATION, "Test");
        Message testReclaim = new Message(MessageKind.RECLAIM, "Test", order);
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
