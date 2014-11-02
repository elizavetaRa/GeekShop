package Prototype;

import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by h4llow3En on 30/10/14.
 */
@Entity
public class Article {

    private @Id @GeneratedValue Long artId;
    private String artName;
    private float artPrice;
    private String artDescription;
    private long artQuantity;

    public Article(String artName, float artPrice, String artDescription, long artQuantity){
        Assert.hasText(artName, "Artikelname fehlt.");
        Assert.notNull(artPrice, "Preis fehlt.");

        if (artDescription == ""){
            artDescription = "No Description.";
        }

        this.artName = artName;
        this.artPrice = artPrice;
        this.artDescription = artDescription;
        this.artQuantity = artQuantity;

    }
    Article(){}

    public String getArtName(){return artName;}
    public String getArtDescription(){return artDescription;}
    public Long getArtId(){return artId;}
    public long getArtQuantity(){return artQuantity;}
    public float getArtPrice(){return artPrice;}
}
