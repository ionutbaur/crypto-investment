package com.ionutzbaur.crypto.investment.util;

import com.ionutzbaur.crypto.investment.domain.CryptoType;
import com.ionutzbaur.crypto.investment.domain.CsvCrypto;
import com.ionutzbaur.crypto.investment.exception.CryptoInvestmentException;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.*;
import java.util.List;

public class CsvCryptoUtil {

    private static final String CSV_BASE_PATH = "src/main/resources/prices/";
    private static final String CSV_SUFFIX = "_values.csv";
    private static final String FILE_PATH = CSV_BASE_PATH + "%s" + CSV_SUFFIX;

    private CsvCryptoUtil() {
        // utility class
    }

    public static CsvToBean<CsvCrypto> readCryptoFromCsv(CryptoType cryptoType) throws IOException {
        final Reader reader = Files.newBufferedReader(Paths.get(String.format(FILE_PATH, cryptoType)));
        return new CsvToBeanBuilder<CsvCrypto>(reader)
                .withType(CsvCrypto.class)
                .withIgnoreLeadingWhiteSpace(true)
                .build();
    }

    public static void writeCryptoToCsv(List<CsvCrypto> csvCryptoList, CryptoType cryptoType) {
        Path outputPath = Paths.get(String.format(FILE_PATH, cryptoType));
        if (Files.exists(outputPath)) {
            writeCryptoToCsv(csvCryptoList, outputPath, false, StandardOpenOption.APPEND);
        } else {
            writeCryptoToCsv(csvCryptoList, outputPath, true);
        }
    }

    public static void writeCryptoToCsv(List<CsvCrypto> csvCryptoList, Path outputPath, boolean writeHeader, OpenOption... options) {
        try (var writer = Files.newBufferedWriter(outputPath, options)) {
            StatefulBeanToCsv<CsvCrypto> csv = new StatefulBeanToCsvBuilder<CsvCrypto>(writer)
                    .withMappingStrategy(new CsvAnnotationStrategy<>(CsvCrypto.class, writeHeader))
                    .withApplyQuotesToAll(false)
                    .build();
            csv.write(csvCryptoList);
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new CryptoInvestmentException(e);
        }
    }

}
