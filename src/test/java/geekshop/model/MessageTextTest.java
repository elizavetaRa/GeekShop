package geekshop.model;

import geekshop.AbstractIntegrationTests;
import org.junit.Test;

/**
 * Empty MessageText Test for {@link geekshop.model.MessageRepository}.
 *
 * @author Felix Döring
 */

public class MessageTextTest extends AbstractIntegrationTests {

    @Test(expected = IllegalArgumentException.class)
    public void emptyMessage() {
        new Message(MessageKind.NOTIFICATION, "");
    }

}
