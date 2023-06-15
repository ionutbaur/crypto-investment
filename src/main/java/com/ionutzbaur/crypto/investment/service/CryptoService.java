package com.ionutzbaur.crypto.investment.service;

import com.ionutzbaur.crypto.investment.domain.CsvCrypto;
import com.ionutzbaur.crypto.investment.domain.CryptoType;
import com.ionutzbaur.crypto.investment.domain.StatisticType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface CryptoService {

    /**
     * Reads the crypto info from all the CSV files.
     * @return a list that represents the deserialized info from CSVs
     */
    List<CsvCrypto> getAllCryptos();

    /**
     * Calculates the normalized range of all cryptos by formula (max-min)/min
     * @return a descending sorted list comparing the calculated normalized range
     */
    List<CsvCrypto> getNormalizedDesc();

    /**
     * Computes statistics
     * @param cryptoType
     * @param statisticType
     * @return the info to match the requested statistic
     */
    CsvCrypto getStatistic(CryptoType cryptoType, StatisticType statisticType);

    /**
     * Computes the highest normalized range for a specific day.
     * @param day the day for witch the highest normalized range is computed
     * @return the symbol of the crypto
     */
    CryptoType getHighestNormalizedRange(LocalDate day);

    /**
     * Adds crypto info. If a csv file for the new crypto does not exist, it will be created,
     * otherwise the info will be appended to the end of the corresponding csv file content.
     * In order to add a new crypto, make sure it is supported by adding it first to {@link CryptoType} enum.
     * @param cryptoValues list of cryptos to be added
     */
    void addCrypto(List<CsvCrypto> cryptoValues);

}
