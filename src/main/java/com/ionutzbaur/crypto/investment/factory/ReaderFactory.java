package com.ionutzbaur.crypto.investment.factory;

import com.ionutzbaur.crypto.investment.exception.CryptoInvestmentException;
import com.ionutzbaur.crypto.investment.domain.CryptoType;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ReaderFactory {

    private static final String BASE_PATH = "src/main/resources/prices/";

    public static Reader getReaderFor(CryptoType cryptoType) throws IOException {
        String fullPath;

        // TODO: consider to refactor the way the path is built (enum + _values.csv)
        switch (cryptoType) {
            case BTC:
                fullPath = BASE_PATH + "BTC_values.csv";
                break;
            case DOGE:
                fullPath = BASE_PATH + "DOGE_values.csv";
                break;
            case ETH:
                fullPath = BASE_PATH + "ETH_values.csv";
                break;
            case LTC:
                fullPath = BASE_PATH + "LTC_values.csv";
                break;
            case XRP:
                fullPath = BASE_PATH + "XRP_values.csv";
                break;
            default:
                throw new CryptoInvestmentException("Crypto not yet supported!");
        }

        return Files.newBufferedReader(Paths.get(fullPath));
    }
}
