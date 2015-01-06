package geekshop.model;

import org.springframework.util.Assert;

import javax.persistence.*;

/**
 * Class representing a message to the owner displayed on {@code /messages}.
 *
 * @author Felix Döring
 */

@Entity
public class Message {
    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private MessageKind messageKind;
    private String messageText;

    private String reclaimId;


    @Deprecated
    protected Message() {

    }

    /**
     * Creates a new {@link geekshop.model.Message} from {@link geekshop.model.MessageKind} NOTIFICATION or PASSWORD.
     * @param messageKind the kind of the Message
     * @param messageText the text of the Message
     */
    public Message(MessageKind messageKind, String messageText) {
        Assert.hasText(messageText, "messageText must not be null.");
        Assert.notNull(messageKind, "messageKind must not be null.");
        this.messageKind = messageKind;
        this.messageText = messageText;

    }

    /**
     * Creates a new {@link geekshop.model.Message} from {@link geekshop.model.MessageKind} RECLAIM.
     * @param messageKind the kind of the Message
     * @param messageText Messagekind RECLAIM
     * @param reclaimId the Id of the {@link geekshop.model.GSOrder} with {@link geekshop.model.OrderType} RECLAIM
     */
    public Message(MessageKind messageKind, String messageText, GSOrder reclaimId) {
        Assert.hasText(messageText, "messageText must not be null.");
        Assert.notNull(messageKind, "messageKind must not be null.");
        Assert.notNull(reclaimId, "reclaimID must not be null.");
        this.messageKind = messageKind;
        this.messageText = messageText;

        if (messageKind == MessageKind.RECLAIM) {
            this.reclaimId = reclaimId.getIdentifier().toString();
        } else {
            this.reclaimId = null;
        }

    }

    public MessageKind getMessageKind() {
        return messageKind;
    }

    public String getMessageText() {
        return messageText;
    }

    public Long getId() {
        return id;
    }

    public String getReclaimId() {
        return reclaimId;
    }
}
