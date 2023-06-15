package com.ionutzbaur.crypto.investment.service.impl;

import com.ionutzbaur.crypto.investment.domain.CryptoType;
import com.ionutzbaur.crypto.investment.domain.CsvCrypto;
import com.ionutzbaur.crypto.investment.domain.StatisticType;
import com.ionutzbaur.crypto.investment.exception.CryptoInvestmentException;
import com.ionutzbaur.crypto.investment.service.CryptoService;
import com.ionutzbaur.crypto.investment.util.CsvCryptoUtil;
import com.opencsv.bean.CsvToBean;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CryptoServiceImpl implements CryptoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoServiceImpl.class);

    /**
     * Reads the crypto info from all the CSV files.
     * @return a list that represents the deserialized info from CSVs
     */
    @Override
    public List<CsvCrypto> getAllCryptos() {
        List<CsvCrypto> allCryptos = new LinkedList<>();
        Arrays.stream(CryptoType.values())
                .forEach(cryptoType -> {
                    try {
                        final CsvToBean<CsvCrypto> crypto = CsvCryptoUtil.readCryptoFromCsv(cryptoType);
                        allCryptos.addAll(crypto.parse());
                    } catch (Exception e) {
                        LOGGER.error("Cannot retrieve info for " + cryptoType, e);
                    }
                });

        return allCryptos;
    }

    /**
     * Calculates the normalized range of all cryptos by formula (max-min)/min
     * @return a descending sorted list comparing the calculated normalized range
     */
    @Override
    public List<CsvCrypto> getNormalizedDesc() {
        final Supplier<Stream<CsvCrypto>> streamSupplier = getAllCryptos()::stream;
        final Comparator<CsvCrypto> comparator = (o1, o2) -> o1.getPrice().compareTo(computeNormalizedRange(streamSupplier));

        return streamSupplier.get()
                .sorted(comparator.reversed())
                .collect(Collectors.toList());
    }

    /**
     * Computes statistics
     * @param cryptoType
     * @param statisticType
     * @return the info to match the requested statistic
     */
    @Override
    public CsvCrypto getStatistic(CryptoType cryptoType, StatisticType statisticType) {
        try {
            final CsvToBean<CsvCrypto> crypto = CsvCryptoUtil.readCryptoFromCsv(cryptoType);
            final Supplier<Stream<CsvCrypto>> streamSupplier = crypto::stream;

            Optional<CsvCrypto> optionalCSVCrypto;
            switch (statisticType) {
                case OLDEST:
                    optionalCSVCrypto = computeOldestTimestampOptional(streamSupplier);
                    break;
                case NEWEST:
                    optionalCSVCrypto = computeNewestTimestampOptional(streamSupplier);
                    break;
                case MIN:
                    optionalCSVCrypto = computeMinPriceOptional(streamSupplier);
                    break;
                case MAX:
                    optionalCSVCrypto = computeMaxPriceOptional(streamSupplier);
                    break;
                default:
                    throw new CryptoInvestmentException("Statistic not yet implemented");
            }

            return optionalCSVCrypto
                    .orElseThrow(() -> new CryptoInvestmentException(String.format("Cannot compute %s values for %s", statisticType, cryptoType)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Computes the highest normalized range for a specific day.
     * @param day the day for witch the highest normalized range is computed
     * @return the symbol of the crypto
     */
    @Override
    public CryptoType getHighestNormalizedRange(LocalDate day) {
        final List<Pair<CryptoType, BigDecimal>> normalizedRangePairs = new ArrayList<>();

        getAllCryptos().stream() //filter by given day
                .filter(csvCrypto -> day.isEqual(LocalDate.ofInstant(csvCrypto.getPrettyTimestamp(), ZoneId.systemDefault())))
                .collect(Collectors.groupingBy(CsvCrypto::getSymbol))
                .forEach((key, value) -> {
                    final BigDecimal normalizedRange = computeNormalizedRange(value::stream);
                    final Pair<CryptoType, BigDecimal> normalizedRangePair = Pair.of(key, normalizedRange);

                    normalizedRangePairs.add(normalizedRangePair);
                });

        return normalizedRangePairs.stream()
                .max(Comparator.comparing(Pair::getRight))
                .map(Pair::getLeft)
                .orElse(null);
    }

    /**
     * Adds crypto info. If a csv file for the new crypto does not exist, it will be created,
     * otherwise the info will be appended at the end of the corresponding csv file content.
     * In order to add a new crypto, make sure it is supported by adding it first to {@link CryptoType} enum.
     * @param cryptoValues list of cryptos to be added
     */
    @Override
    public void addCrypto(List<CsvCrypto> cryptoValues) {
        cryptoValues.stream()
                .collect(Collectors.groupingBy(CsvCrypto::getSymbol))
                .forEach((cryptoType, csvCryptoList) -> CsvCryptoUtil.writeCryptoToCsv(csvCryptoList, cryptoType));
    }

    private BigDecimal computeNormalizedRange(Supplier<Stream<CsvCrypto>> streamSupplier) {
        final double maxPrice = computeMaxPriceOptional(streamSupplier)
                .orElseThrow(() -> new CryptoInvestmentException("Cannot compute max price!"))
                .getPrice()
                .doubleValue();

        final double minPrice = computeMinPriceOptional(streamSupplier)
                .orElseThrow(() -> new CryptoInvestmentException("Cannot compute min price!"))
                .getPrice()
                .doubleValue();

        return BigDecimal
                .valueOf((maxPrice - minPrice) / minPrice)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Optional<CsvCrypto> computeMinPriceOptional(Supplier<Stream<CsvCrypto>> streamSupplier) {
        return streamSupplier.get()
                .min(comparePrice());
    }

    private Optional<CsvCrypto> computeMaxPriceOptional(Supplier<Stream<CsvCrypto>> streamSupplier) {
        return streamSupplier.get()
                .max(comparePrice());
    }

    private Optional<CsvCrypto> computeOldestTimestampOptional(Supplier<Stream<CsvCrypto>> streamSupplier) {
        return streamSupplier.get()
                .min(compareTimestamp());
    }

    private Optional<CsvCrypto> computeNewestTimestampOptional(Supplier<Stream<CsvCrypto>> streamSupplier) {
        return streamSupplier.get()
                .max(compareTimestamp());
    }

    private static Comparator<CsvCrypto> comparePrice() {
        return Comparator.comparing(CsvCrypto::getPrice);
    }

    private static Comparator<CsvCrypto> compareTimestamp() {
        return Comparator.comparing(CsvCrypto::getTimestamp);
    }

}
