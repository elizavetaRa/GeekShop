package geekshop.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Class representing a joke displayed after user login. Jokes are stored in the {@link geekshop.model.JokeRepository}.
 *
 * @author Sebastian D&ouml;ring
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
