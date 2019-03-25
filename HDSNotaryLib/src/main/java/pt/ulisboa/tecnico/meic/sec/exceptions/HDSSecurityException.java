package pt.ulisboa.tecnico.meic.sec.exceptions;

/**
 * This class is thrown by Notary when security problems occur.
 * Security problems are related to cryptographic mechanisms or Cartão do Cidadão operations.
 */
public class HDSSecurityException extends Exception {

    public HDSSecurityException() {
        super();
    }

    public HDSSecurityException(String message) {
        super(message);
    }
}
