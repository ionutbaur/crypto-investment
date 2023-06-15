package com.ionutzbaur.crypto.investment.service.impl;

import com.ionutzbaur.crypto.investment.domain.CryptoType;
import com.ionutzbaur.crypto.investment.domain.CsvCrypto;
import com.ionutzbaur.crypto.investment.domain.StatisticType;
import com.ionutzbaur.crypto.investment.service.CryptoService;
import com.ionutzbaur.crypto.investment.util.CsvCryptoUtil;
import com.opencsv.bean.CsvToBean;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class CryptoServiceImplTest {

    private static final double MAX_PRICE = 46813.21d;
    private static final double MIN_PRICE = 0.1702d;
    private static final double MID_PRICE = 3715.32d;

    private static final long NEWEST_CRYPTO = Instant.now().toEpochMilli();
    private static final long OLDEST_CRYPTO = LocalDateTime.now().minusDays(10).toInstant(ZoneOffset.UTC).toEpochMilli();

    @Mock
    private CsvCrypto btcCsvCrypto;

    @Mock
    private CsvCrypto otherBtcCsvCrypto;

    @Mock
    private CsvCrypto ethCsvCrypto;

    @Mock
    private CsvCrypto dogeCsvCrypto;

    @Mock
    private CsvToBean<CsvCrypto> btcCsvCryptoBean;

    @Mock
    private CsvToBean<CsvCrypto> ethCsvCryptoBean;

    @Mock
    private CsvToBean<CsvCrypto> dogeCsvCryptoBean;

    private MockedStatic<CsvCryptoUtil> csvCryptoUtilMockedStatic;

    private List<CsvCrypto> btcCryptoList;
    private List<CsvCrypto> ethCryptoList;
    private List<CsvCrypto> dogeCsvCryptoList;

    private final CryptoService cryptoService = new CryptoServiceImpl();

    @BeforeEach
    void setUp() {
        btcCryptoList = List.of(btcCsvCrypto);
        ethCryptoList = List.of(ethCsvCrypto);
        dogeCsvCryptoList = List.of(dogeCsvCrypto);
        csvCryptoUtilMockedStatic = mockStatic(CsvCryptoUtil.class);
    }

    @AfterEach
    void tearDown() {
        csvCryptoUtilMockedStatic.close();
    }

    @Test
    void getAllCryptos() {
        final List<CsvCrypto> btcCryptoList = List.of(btcCsvCrypto);
        final List<CsvCrypto> ethCryptoList = List.of(ethCsvCrypto);

        when(btcCsvCryptoBean.parse()).thenReturn(btcCryptoList);
        when(ethCsvCryptoBean.parse()).thenReturn(ethCryptoList);

        final List<CsvCrypto> expectedList = new ArrayList<>();
        expectedList.addAll(btcCryptoList);
        expectedList.addAll(ethCryptoList);

        csvCryptoUtilMockedStatic.when(() -> CsvCryptoUtil.readCryptoFromCsv(CryptoType.BTC))
                .thenReturn(btcCsvCryptoBean);
        csvCryptoUtilMockedStatic.when(() -> CsvCryptoUtil.readCryptoFromCsv(CryptoType.ETH))
                .thenReturn(ethCsvCryptoBean);

        List<CsvCrypto> result = cryptoService.getAllCryptos();
        assertEquals(expectedList, result);
    }

    @Test
    void getNormalizedDesc() {
        when(btcCsvCryptoBean.parse()).thenReturn(btcCryptoList);
        when(ethCsvCryptoBean.parse()).thenReturn(ethCryptoList);
        when(dogeCsvCryptoBean.parse()).thenReturn(dogeCsvCryptoList);

        when(btcCsvCrypto.getPrice()).thenReturn(BigDecimal.valueOf(MAX_PRICE));
        when(ethCsvCrypto.getPrice()).thenReturn(BigDecimal.valueOf(MID_PRICE));
        when(dogeCsvCrypto.getPrice()).thenReturn(BigDecimal.valueOf(MIN_PRICE));

        csvCryptoUtilMockedStatic.when(() -> CsvCryptoUtil.readCryptoFromCsv(CryptoType.BTC))
                .thenReturn(btcCsvCryptoBean);
        csvCryptoUtilMockedStatic.when(() -> CsvCryptoUtil.readCryptoFromCsv(CryptoType.ETH))
                .thenReturn(ethCsvCryptoBean);
        csvCryptoUtilMockedStatic.when(() -> CsvCryptoUtil.readCryptoFromCsv(CryptoType.DOGE))
                .thenReturn(dogeCsvCryptoBean);

        List<CsvCrypto> result = cryptoService.getNormalizedDesc();

        final double normalizedRange = (MAX_PRICE - MIN_PRICE) / MIN_PRICE;
        final Comparator<CsvCrypto> comparator = Comparator.comparing(CsvCrypto::getPrice)
                .thenComparing((o1, o2) -> o1.getPrice().compareTo(BigDecimal.valueOf(normalizedRange)));

        List<CsvCrypto> expectedList = Stream.of(btcCsvCrypto, ethCsvCrypto, dogeCsvCrypto)
                .sorted(comparator.reversed())
                .collect(Collectors.toList());
        assertEquals(expectedList, result);
    }

    @Test
    void getStatistic_max() {
        assertEquals(btcCsvCrypto, getPriceStatistic(StatisticType.MAX));
    }

    @Test
    void getStatistic_min() {
        assertEquals(otherBtcCsvCrypto, getPriceStatistic(StatisticType.MIN));
    }

    private CsvCrypto getPriceStatistic(StatisticType statisticType) {
        when(btcCsvCrypto.getPrice()).thenReturn(BigDecimal.valueOf(MAX_PRICE));
        when(otherBtcCsvCrypto.getPrice()).thenReturn(BigDecimal.valueOf(MIN_PRICE));

        when(btcCsvCryptoBean.stream()).thenReturn(Stream.of(btcCsvCrypto, otherBtcCsvCrypto));

        csvCryptoUtilMockedStatic.when(() -> CsvCryptoUtil.readCryptoFromCsv(CryptoType.BTC))
                .thenReturn(btcCsvCryptoBean);

        return cryptoService.getStatistic(CryptoType.BTC, statisticType);
    }

    @Test
    void getStatistic_newest() {
        assertEquals(btcCsvCrypto, getTimestampStatistic(StatisticType.NEWEST));
    }

    @Test
    void getStatistic_oldest() {
        assertEquals(otherBtcCsvCrypto, getTimestampStatistic(StatisticType.OLDEST));
    }

    private CsvCrypto getTimestampStatistic(StatisticType statisticType) {
        when(btcCsvCrypto.getTimestamp()).thenReturn(NEWEST_CRYPTO);
        when(otherBtcCsvCrypto.getTimestamp()).thenReturn(OLDEST_CRYPTO);

        when(btcCsvCryptoBean.stream()).thenReturn(Stream.of(btcCsvCrypto, otherBtcCsvCrypto));

        csvCryptoUtilMockedStatic.when(() -> CsvCryptoUtil.readCryptoFromCsv(CryptoType.BTC))
                .thenReturn(btcCsvCryptoBean);

        return cryptoService.getStatistic(CryptoType.BTC, statisticType);
    }

    @Test
    void getHighestNormalizedRange() {
        when(btcCsvCrypto.getPrettyTimestamp()).thenReturn(Instant.now());
        when(btcCsvCrypto.getSymbol()).thenReturn(CryptoType.BTC);
        when(btcCsvCrypto.getPrice()).thenReturn(BigDecimal.valueOf(MAX_PRICE));

        when(otherBtcCsvCrypto.getPrettyTimestamp()).thenReturn(Instant.now());
        when(otherBtcCsvCrypto.getSymbol()).thenReturn(CryptoType.BTC);
        when(otherBtcCsvCrypto.getPrice()).thenReturn(BigDecimal.valueOf(MID_PRICE));

        when(ethCsvCrypto.getPrettyTimestamp()).thenReturn(Instant.now());
        when(ethCsvCrypto.getSymbol()).thenReturn(CryptoType.ETH);
        when(ethCsvCrypto.getPrice()).thenReturn(BigDecimal.valueOf(MIN_PRICE));

        when(btcCsvCryptoBean.parse()).thenReturn(List.of(btcCsvCrypto, otherBtcCsvCrypto));
        when(ethCsvCryptoBean.parse()).thenReturn(ethCryptoList);

        csvCryptoUtilMockedStatic.when(() -> CsvCryptoUtil.readCryptoFromCsv(CryptoType.BTC))
                .thenReturn(btcCsvCryptoBean);
        csvCryptoUtilMockedStatic.when(() -> CsvCryptoUtil.readCryptoFromCsv(CryptoType.ETH))
                .thenReturn(ethCsvCryptoBean);

        var todayBtcMax = Stream.of(btcCsvCrypto, otherBtcCsvCrypto)
                .max(Comparator.comparing(CsvCrypto::getPrice))
                .get()
                .getPrice()
                .doubleValue(); // MAX_PRICE
        var todayBtcMin = Stream.of(btcCsvCrypto, otherBtcCsvCrypto)
                .min(Comparator.comparing(CsvCrypto::getPrice))
                .get()
                .getPrice()
                .doubleValue(); // MID_PRICE

        BigDecimal normalizedTodayBtcRange = BigDecimal.valueOf((todayBtcMax - todayBtcMin) / todayBtcMin)
                .setScale(2, RoundingMode.HALF_UP);

        var todayEthMax = ethCsvCrypto.getPrice().doubleValue(); // MIN_PRICE
        var todayEthMin = ethCsvCrypto.getPrice().doubleValue(); // same MIN_PRICE

        BigDecimal normalizedTodayEthRange = BigDecimal.valueOf((todayEthMax - todayEthMin) / todayEthMin)
                .setScale(2, RoundingMode.HALF_UP);

        final Pair<CryptoType, BigDecimal> btcPair = Pair.of(CryptoType.BTC, normalizedTodayBtcRange);
        final Pair<CryptoType, BigDecimal> ethPair = Pair.of(CryptoType.ETH, normalizedTodayEthRange);

        CryptoType expectedCryptoType = Stream.of(btcPair, ethPair)
                .max(Comparator.comparing(Pair::getRight))
                .map(Pair::getLeft)
                .orElse(null);

        CryptoType result = cryptoService.getHighestNormalizedRange(LocalDate.now());
        assertEquals(expectedCryptoType, result); // BTC
    }

    @Test
    void addCrypto() {
        when(btcCsvCrypto.getSymbol()).thenReturn(CryptoType.BTC);
        when(ethCsvCrypto.getSymbol()).thenReturn(CryptoType.ETH);

        List<CsvCrypto> csvCryptoList = List.of(btcCsvCrypto, ethCsvCrypto);
        cryptoService.addCrypto(csvCryptoList);

        csvCryptoUtilMockedStatic.verify(() -> CsvCryptoUtil.writeCryptoToCsv(List.of(btcCsvCrypto), CryptoType.BTC));
        csvCryptoUtilMockedStatic.verify(() -> CsvCryptoUtil.writeCryptoToCsv(List.of(ethCsvCrypto), CryptoType.ETH));
    }

}