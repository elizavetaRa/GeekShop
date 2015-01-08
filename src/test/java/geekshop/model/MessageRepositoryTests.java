package geekshop.model;

import geekshop.AbstractIntegrationTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Integration tests for {@link geekshop.model.MessageRepository}.
 *
 * @author Felix Döring
 */

public class MessageRepositoryTests extends AbstractIntegrationTests {

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

    @Test
    public void testFindByMessageKind() {
        Message not = new Message(MessageKind.NOTIFICATION, "notification");
        Message recl = new Message(MessageKind.RECLAIM, "reclaim");
        Message pw = new Message(MessageKind.PASSWORD, "password");
        messageRepo.save(Arrays.asList(not, recl, pw));

        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), hasItem(not));
        assertThat(messageRepo.findByMessageKind(MessageKind.NOTIFICATION), both(not(hasItem(recl))).and(not(hasItem(pw))));
        assertThat(messageRepo.findByMessageKind(MessageKind.RECLAIM), hasItem(recl));
        assertThat(messageRepo.findByMessageKind(MessageKind.RECLAIM), both(not(hasItem(not))).and(not(hasItem(pw))));
        assertThat(messageRepo.findByMessageKind(MessageKind.PASSWORD), hasItem(pw));
        assertThat(messageRepo.findByMessageKind(MessageKind.PASSWORD), both(not(hasItem(not))).and(not(hasItem(recl))));
    }

}
