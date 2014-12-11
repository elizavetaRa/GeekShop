package geekshop.model;

/*
 * Created by Basti on 24.11.2014.
 */

/**
 * Enumeration for the marital status of the {@link User}s.
 *
 * @author Sebastian D&ouml;ring
 */

public enum MaritalStatus {
    UNMARRIED("ledig"), MARRIED("verheiratet"), SEPARATED("getrennt lebend"), DIVORCED("geschieden"), WIDOWED("verwitwet"),
    PARTNERED("verpartnert"), NO_MORE_PARTNERED("entpartnert"), PARTNER_LEFT_BEHIND("partnerhinterblieben"),
    UNKNOWN("unbekannt");

    private String value;

    private MaritalStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
