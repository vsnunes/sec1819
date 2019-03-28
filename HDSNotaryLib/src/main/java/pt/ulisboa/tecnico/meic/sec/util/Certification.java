package pt.ulisboa.tecnico.meic.sec.util;

import pt.ulisboa.tecnico.meic.sec.exceptions.HDSSecurityException;

/**
 * Interface to allow multiple certification methods
 */
public interface Certification {

    /** init operations for this certification method **/
    void init(String... args) throws HDSSecurityException;

    /** stop operations for this certification method **/
    void stop() throws HDSSecurityException;

    /** Default signData method **/
    byte[] signData(byte[] data) throws HDSSecurityException;

    /** Verify a data signature **/
    boolean verifyData(byte[] expected, byte[] original) throws HDSSecurityException;


}
