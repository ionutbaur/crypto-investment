package com.ionutzbaur.crypto.investment.util;

import com.ionutzbaur.crypto.investment.factory.ReaderFactory;
import com.ionutzbaur.crypto.investment.domain.CsvCrypto;
import com.ionutzbaur.crypto.investment.domain.CryptoType;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.IOException;
import java.io.Reader;

public class CryptoReaderUtil {

    private CryptoReaderUtil() {
        // utility class
    }

    public static CsvToBean<CsvCrypto> readCryptoFromCsv(CryptoType cryptoType) throws IOException {
        final Reader reader = ReaderFactory.getReaderFor(cryptoType);
        return new CsvToBeanBuilder<CsvCrypto>(reader)
                .withType(CsvCrypto.class)
                .withIgnoreLeadingWhiteSpace(true)
                .build();
    }
}
