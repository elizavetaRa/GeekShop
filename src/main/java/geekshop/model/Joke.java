package GeekShop.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
* Created by Basti on 24.11.2014.
*/

@Entity
public class Joke {
    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 512 * 1024)
    private String text;


    @Deprecated
    protected Joke() {}

    public Joke(String text) {
        this.text = text;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
