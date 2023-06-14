package com.ionutzbaur.crypto.investment.service.impl;

import com.ionutzbaur.crypto.investment.domain.CryptoType;
import com.ionutzbaur.crypto.investment.domain.CsvCrypto;
import com.ionutzbaur.crypto.investment.domain.StatisticType;
import com.ionutzbaur.crypto.investment.exception.CryptoInvestmentException;
import com.ionutzbaur.crypto.investment.service.CryptoReaderService;
import com.ionutzbaur.crypto.investment.util.CryptoReaderUtil;
import com.opencsv.bean.CsvToBean;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CryptoReaderServiceImpl implements CryptoReaderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoReaderServiceImpl.class);

    @Override
    public List<CsvCrypto> getAllCryptos() {
        List<CsvCrypto> allCryptos = new LinkedList<>();
        Arrays.stream(CryptoType.values())
                .forEach(cryptoType -> {
                    try {
                        final CsvToBean<CsvCrypto> crypto = CryptoReaderUtil.readCryptoFromCsv(cryptoType);
                        allCryptos.addAll(crypto.parse());
                    } catch (Exception e) {
                        LOGGER.error("Cannot retrieve info for " + cryptoType, e);
                    }
                });

        return allCryptos;
    }

    @Override
    public List<CsvCrypto> getNormalizedDesc() {
        final Supplier<Stream<CsvCrypto>> streamSupplier = getAllCryptos()::stream;
        final Comparator<CsvCrypto> comparator = (o1, o2) -> o1.getPrice().compareTo(computeNormalizedRange(streamSupplier));

        return streamSupplier.get()
                .sorted(comparator.reversed())
                .collect(Collectors.toList());
    }

    @Override
    public CsvCrypto getStatistic(CryptoType cryptoType, StatisticType statisticType) {
        try {
            final CsvToBean<CsvCrypto> crypto = CryptoReaderUtil.readCryptoFromCsv(cryptoType);
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
