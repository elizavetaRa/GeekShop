package geekshop.model;

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
    private String phone;
    private String street;
    private String houseNr;
    private String postcode;
    private String place;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;

    @ManyToMany
    private List<Joke> recentJokes;

    @OneToOne(cascade = CascadeType.ALL)
    private PasswordAttributes passwordAttributes;


    @Deprecated
    protected User() {
        this.recentJokes = new LinkedList<Joke>();
    }

    public User(UserAccount userAccount, String password, Gender gender, Date birthday,
                MaritalStatus maritalStatus, String phone,
                String street, String houseNr, String postcode, String place) {
        Assert.notNull(userAccount, "UserAccount must not be null.");
        this.userAccount = userAccount;
        this.gender = gender;
        this.birthday = birthday;
        this.maritalStatus = maritalStatus;
        this.phone = phone;
        this.street = street;
        this.houseNr = houseNr;
        this.postcode = postcode;
        this.place = place;

        this.recentJokes = new LinkedList<Joke>();
        this.passwordAttributes = new PasswordAttributes(
                PasswordRules.containsUpperAndLower(password), PasswordRules.containsDigits(password),
                PasswordRules.containsSpecialCharacters(password), password.length());
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
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

    public Long getId() {
        return id;
    }

    public PasswordAttributes getPasswordAttributes() {
        return passwordAttributes;
    }

    public void setPasswordAttributes(PasswordAttributes passwordAttributes) {
        this.passwordAttributes = passwordAttributes;
    }

    public String toString() {
        return userAccount.getFirstname() + " " + userAccount.getLastname();
    }
}
