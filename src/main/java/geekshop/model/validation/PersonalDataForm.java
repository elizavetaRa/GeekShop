package geekshop.model.validation;

import geekshop.model.Gender;
import geekshop.model.MaritalStatus;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Validation class for form changing personal data.
 *
 * @author Sebastian Döring
 */
public class PersonalDataForm {

    @NotBlank(message = "Kein Vorname angegeben.")
    private String firstname;

    @NotBlank(message = "Kein Nachname angegeben.")
    private String lastname;

    @NotNull(message = "Kein Benutzername angegeben.")
    @Size(min = 3, message = "Benutzername muss mind. 3 Zeichen lang sein.")
    @Pattern(regexp = "[^\\s]*", message = "Benutzername darf keine Leerzeichen enthalten.")
    private String username;

    @NotEmpty(message = "Keine E-Mail-Adresse angegeben.")
    @Email(message = "Ungültige E-Mail-Adresse angegeben.")
    private String email;

    @NotNull(message = "Geschlecht nicht angegeben.")
    private Gender gender;

    @NotEmpty(message = "Geburtsdatum nicht angegeben.")
    @DateOfBirth(message = "Ungültiges Datum angegeben.")
    private String dateOfBirth;

    @NotNull(message = "Familienstand nicht angegeben.")
    private MaritalStatus maritalStatus;

    @NotNull(message = "Keine Telefonnummer angegeben.")
    @Pattern(regexp = "\\s*(\\+?(\\d\\s?){2,6}[\\s-])?(\\(0\\)\\s?(\\d\\s?){2,5}|\\(?(\\d\\s?){3,6}\\)?)\\s?[-/]?\\s?(\\d[\\s-]?){5,11}\\s*", message = "Ungültige Telefonnummer.")
    private String phone;

    @NotBlank(message = "Keine Straße angegeben.")
    private String street;

    @NotNull(message = "Keine Hausnummer angegeben.")
    @Pattern(regexp = ".*\\d.*", message = "Ungültige Hausnummer angegeben.")
    private String houseNr;

    @NotNull(message = "Keine PLZ angegeben.")
    @Pattern(regexp = "\\s*\\d{5}\\s*", message = "Ungültige PLZ angegeben.")
    private String postcode;

    @NotBlank(message = "Kein Wohnort angegeben.")
    private String place;

    public PersonalDataForm() {}

    public PersonalDataForm(String firstname, String lastname, String username, String email, Gender gender, String dateOfBirth,
                            MaritalStatus maritalStatus, String phone, String street, String houseNr, String postcode, String place) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.email = email;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.maritalStatus = maritalStatus;
        this.phone = phone;
        this.street = street;
        this.houseNr = houseNr;
        this.postcode = postcode;
        this.place = place;
    }


    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
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
}
