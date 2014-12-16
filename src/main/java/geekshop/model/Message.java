package geekshop.model;

import org.salespointframework.order.OrderIdentifier;
import org.springframework.util.Assert;

import javax.persistence.*;

/**
 * Class representing a message to the owner displayed on Messages. Messages are stored in the {@link geekshop.model.MessageRepository}.
 *
 * @author Felix D&ouml;ring
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

    public Message(MessageKind messageKind, String messageText) {
        Assert.hasText(messageText, "messageText must not be null.");
        Assert.notNull(messageKind, "messageKind must not be null.");
        this.messageKind = messageKind;
        this.messageText = messageText;

    }

    public Message(MessageKind messageKind, String messageText, OrderIdentifier reclaimId) {
        Assert.hasText(messageText, "messageText must not be null.");
        Assert.notNull(messageKind, "messageKind must not be null.");
        this.messageKind = messageKind;
        this.messageText = messageText;

        if (messageKind == MessageKind.RECLAIM) {
            this.reclaimId = reclaimId.toString();
        } else {
            this.reclaimId = null;
        }

    }


    public String kindToString() {
        if (messageKind.equals(MessageKind.NOTIFICATION)) return "NOTIFICATION";
        else return "RECLAIM";
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
