package geekshop.model;

import geekshop.AbstractIntegrationTests;
import org.junit.Test;

/**
 * Empty ReclaimLink Test for {@link geekshop.model.MessageRepository}.
 *
 * @author Felix Döring
 */

public class MessageReclaimTests extends AbstractIntegrationTests {

    @Test(expected = IllegalArgumentException.class)
    public void emptyReclaimLink() {
        GSOrder identifier = null;
        new Message(MessageKind.RECLAIM, "Test", identifier);
    }

}
