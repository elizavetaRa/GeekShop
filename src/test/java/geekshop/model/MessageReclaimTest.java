package geekshop.model;

import geekshop.GeekShop;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;

/**
 * Empty ReclaimLink Test for {@link geekshop.model.MessageRepository}.
 *
 *
 *
 * @author Felix D&ouml;ring
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = GeekShop.class)
@Transactional
public class MessageReclaimTest {
    @Test(expected = IllegalArgumentException.class)
    public void emptyReclaimLink() {
        GSOrder identifier = null;
        new Message(MessageKind.RECLAIM, "Test", identifier);

    }


}
