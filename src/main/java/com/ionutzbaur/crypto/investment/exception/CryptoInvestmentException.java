package com.ionutzbaur.crypto.investment.exception;

public class CryptoInvestmentException extends RuntimeException {

    public CryptoInvestmentException(String message) {
        super(message);
    }

    public CryptoInvestmentException(Throwable cause) {
        super(cause);
    }
}
