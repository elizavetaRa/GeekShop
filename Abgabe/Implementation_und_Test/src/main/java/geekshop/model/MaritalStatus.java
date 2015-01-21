package geekshop.model;

/**
 * Enumeration representing the {@link User}'s marital status.
 *
 * @author Sebastian Döring
 * @author Felix Döring
 */

public enum MaritalStatus {
    UNMARRIED("ledig"), MARRIED("verheiratet"), SEPARATED("getrennt lebend"), DIVORCED("geschieden"), WIDOWED("verwitwet"),
    PARTNERED("verpartnert"), NO_MORE_PARTNERED("entpartnert"), PARTNER_LEFT_BEHIND("partnerhinterblieben"),
    UNKNOWN("|_||\\||33|<4|\\||\\|7");

    private String value;

    private MaritalStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
