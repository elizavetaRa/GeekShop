package geekshop.model;

import geekshop.GeekShop;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;

/**
 * Empty MessageText Test for {@link geekshop.model.MessageRepository}.
 * <p/>
 * *
 *
 * @author Felix D&ouml;ring
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = GeekShop.class)
@Transactional
public class MessageTextTest {

    @Test(expected = IllegalArgumentException.class)
    public void emptyMessage() {
        new Message(MessageKind.NOTIFICATION, "");

    }


}
