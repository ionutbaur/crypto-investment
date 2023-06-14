package com.ionutzbaur.crypto.investment.domain;

import com.opencsv.bean.CsvBindByName;

import java.math.BigDecimal;
import java.time.Instant;

public class CsvCrypto {

    @CsvBindByName
    private Long timestamp;

    @CsvBindByName
    private CryptoType symbol;

    @CsvBindByName
    private BigDecimal price;

    public Long getTimestamp() {
        return timestamp;
    }

    public CryptoType getSymbol() {
        return symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Instant getPrettyTimestamp() {
        return Instant.ofEpochMilli(timestamp);
    }
}
