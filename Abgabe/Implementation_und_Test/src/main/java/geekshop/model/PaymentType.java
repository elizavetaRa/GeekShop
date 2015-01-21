package geekshop.model;

/**
 * Enumeration representing the payment Method of {@link GSOrder}.
 *
 * @author Elizaveta Ragozina
 */
public enum PaymentType {
    CASH("Barzahlung"), CREDITCARD("Kreditkarte"), CHEQUE("Lastschriftverfahren");

    private String value;

    private PaymentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }


}
