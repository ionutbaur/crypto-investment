package com.ionutzbaur.crypto.investment.service;

import com.ionutzbaur.crypto.investment.domain.CsvCrypto;
import com.ionutzbaur.crypto.investment.domain.CryptoType;
import com.ionutzbaur.crypto.investment.domain.StatisticType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface CryptoReaderService {

    List<CsvCrypto> getAllCryptos();

    List<CsvCrypto> getNormalizedDesc();

    CsvCrypto getStatistic(CryptoType cryptoType, StatisticType statisticType);

    CryptoType getHighestNormalizedRange(LocalDate day);

}
