package geekshop.model;

import org.salespointframework.useraccount.UserAccount;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents the shop's {@link User}s (shop owner and his employees).
 *
 * @author Sebastian Döring
 * @author Felix Döring
 */

@Entity
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    private UserAccount userAccount;

    private Date dateOfBirth;
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

    /**
     * Creates a new {@link User} with the given {@link UserAccount}, password, gender, date of birth, {@link MaritalStatus},
     * phone number, street, house number, postcode and place where he lives.
     * {@link PasswordAttributes} are set by the given password.
     *
     * @param userAccount must not be {@literal null}.
     */
    public User(UserAccount userAccount, String password, Gender gender, Date dateOfBirth,
                MaritalStatus maritalStatus, String phone,
                String street, String houseNr, String postcode, String place) {
        Assert.notNull(userAccount, "UserAccount must not be null.");
        this.userAccount = userAccount;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
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

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String dateOfBirthToString() {
        return new SimpleDateFormat("dd.MM.yyyy").format(dateOfBirth);
    }

    /**
     * Converts a proper {@link java.lang.String} representing a date arranged in the common order of the German-speaking area,
     * e. g. {@code 14.03.95} or {@code 14.03.1995}, to {@link java.util.Date}.
     * Permitted as separators are white-space character, {@literal .}, {@literal -} and {@literal /}.
     * <p>
     * If there are given only two digits for year, the year is completed automatically according to the current century,
     * yet so that the date is lying in the past. For example, {@code xx.xx.99} becomes to {@code xx.xx.1999}.
     *
     * @param strDate the String which contains a Date
     * @return the represented {@link java.util.Date} or {@literal null} if the given String cannot be parsed
     */
    public static Date strToDate(String strDate) {
        if (!strDate.matches("\\d{1,2}[\\s.\\-/]\\d{1,2}[\\s.\\-/]\\d{2}(\\d{2})?"))
            return null;

        strDate = strDate.replace(".", " ");
        strDate = strDate.replace("-", " ");
        strDate = strDate.replace("/", " ");
        if (strDate.matches("\\d{1,2} \\d{1,2} \\d{2}")) { // if date of format "x(x) x(x) xx", complete year to format xxxx
            String dayMonth = strDate.substring(0, strDate.lastIndexOf(' '));
            String year = strDate.substring(dayMonth.length() + 1);
            int yearPrefix = Calendar.getInstance().get(Calendar.YEAR) / 100; // e.g. 19 for 19xx, 20 for 20xx
            if (Integer.parseInt(yearPrefix + year) >= Calendar.getInstance().get(Calendar.YEAR)) // let current year be 2015: 01..14 -> 2014, 15..99 -> 1915
                yearPrefix--;
            strDate = dayMonth + " " + yearPrefix + year;
        }

        Date date = null;
        try {
            date = new SimpleDateFormat("dd MM yyyy").parse(strDate);
        } catch (ParseException ignored) {
        }

        return date;
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

    /**
     * Adds a new {@link Joke} to the list of recent jokes shown to this user.
     * <p>
     * Only the last five jokes are stored. If the given joke is already existing in list, the existing one will be removed.
     */
    public void addJoke(Joke joke) {
        recentJokes.remove(joke); // ensuring that each joke is only once in list
        while (recentJokes.size() >= 5) {
            recentJokes.remove(0);
        }
        recentJokes.add(joke);
    }

    /**
     * Provides the last joke shown or {@literal null} if no jokes were shown currently.
     */
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

    /**
     * Provides the user's full name.
     */
    @Override
    public String toString() {
        return userAccount.getFirstname() + (userAccount.getLastname().isEmpty() ? "" : " " + userAccount.getLastname());
    }
}
