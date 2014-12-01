package geekshop.model;

/*
 * Created by h4llow3En on 20/11/14.
 */

import org.salespointframework.useraccount.UserAccount;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents the shop's users (shop owner and his employees).
 *
 * @author Felix D&ouml;ring
 * @author Sebastian D&ouml;ring
 */

@Entity
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    private UserAccount userAccount;

    private String currentSessionId;

    private Date birthday;
    private String landline;
    private String mobile;
    private String street;
    private String houseNr;
    private String postcode;
    private String place;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Enumerated(EnumType.STRING)
    private MaritalStatus status;

    @ManyToMany
    private List<Joke> recentJokes;


    @Deprecated
    protected User() {
    }

    public User(UserAccount userAccount) {
        Assert.notNull(userAccount, "UserAccount must not be null.");
        this.userAccount = userAccount;

        this.recentJokes = new LinkedList<Joke>();
    }


    public UserAccount getUserAccount() {
        return userAccount;
    }

    public String getCurrentSessionId() {
        return currentSessionId;
    }

    public void setCurrentSessionId(String currentSessionId) {
        this.currentSessionId = currentSessionId;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getLandline() {
        return landline;
    }

    public void setLandline(String landline) {
        this.landline = landline;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouseNr() {
        return houseNr;
    }

    public void setHouseNr(String houseNr) {
        this.houseNr = houseNr;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public MaritalStatus getStatus() {
        return status;
    }

    public void setStatus(MaritalStatus status) {
        this.status = status;
    }

    public List<Joke> getRecentJokes() {
        return recentJokes;
    }

    protected void setRecentJokes(List<Joke> recentJokes) {
        this.recentJokes = recentJokes;
    }

    public void addJoke(Joke joke) {
        recentJokes.remove(joke);
        if (recentJokes.size() == 5) {
            recentJokes.remove(0);
        }
        recentJokes.add(joke);
    }

    public Joke getLastJoke() {
        return recentJokes.size() > 0 ? recentJokes.get(recentJokes.size() - 1) : null;
    }
}
