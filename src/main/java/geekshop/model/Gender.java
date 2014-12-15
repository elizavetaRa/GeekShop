package geekshop.model;

/*
 * Created by Basti on 24.11.2014.
 */

/**
 * Enumeration representing the {@link User}'s gender.
 *
 * @author Sebastian D&ouml;ring
 */

public enum Gender {
    MALE("männlich"), FEMALE("weiblich"), SOMETHING_ELSE("etwas anderes");

    private String value;

    private Gender(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
