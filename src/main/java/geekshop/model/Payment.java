package geekshop.model;

/**
 * Created by Lisa on 19.12.2014.
 */
public enum Payment {
    CASH("Barzahlung"), CREDITCARD("Kreditkarte"), CHEQUE("Lastschriftverfahren");

    private String value;

    private Payment(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }


}
