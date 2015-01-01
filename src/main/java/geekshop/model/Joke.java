package geekshop.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Class representing a joke displayed after user login.
 *
 * @author Sebastian Döring
 */

@Entity
public class Joke {
    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 512 * 1024)
    private String text;


    @Deprecated
    protected Joke() {
    }

    /**
     * Creates a new {@link Joke} with the given text.
     */
    public Joke(String text) {
        this.text = text;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getId() {
        return id;
    }
}
