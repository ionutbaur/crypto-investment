package com.ionutzbaur.crypto.investment.controller;

import com.ionutzbaur.crypto.investment.domain.CryptoType;
import com.ionutzbaur.crypto.investment.domain.CsvCrypto;
import com.ionutzbaur.crypto.investment.domain.StatisticType;
import com.ionutzbaur.crypto.investment.service.CryptoReaderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@RestController
public class CryptoController {

    private final CryptoReaderService cryptoReaderService;

    public CryptoController(CryptoReaderService cryptoReaderService) {
        this.cryptoReaderService = cryptoReaderService;
    }

    @GetMapping("/stat/{crypto}")
    public CsvCrypto getStatisticValues(@PathVariable CryptoType crypto,
                                        @RequestParam(value = "type", defaultValue = "NEWEST") StatisticType statisticType) {
        return cryptoReaderService.getStatistic(crypto, statisticType);
    }

    @GetMapping("/normalize-desc")
    public List<CsvCrypto> getNormalizedDesc() {
        return cryptoReaderService.getNormalizedDesc();
    }

    @GetMapping("/normalize-highest/{day}")
    public CryptoType getHighestNormalizedRange(@PathVariable String day) {
        LocalDate localDate;
        try {
            localDate = LocalDate.parse(day);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a valid date! Format should be YYYY-MM-DD");
        }

        return Optional
                .ofNullable(cryptoReaderService.getHighestNormalizedRange(localDate))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No available data for the given day."));
    }

}
