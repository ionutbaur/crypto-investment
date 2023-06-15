package com.ionutzbaur.crypto.investment.controller;

import com.ionutzbaur.crypto.investment.domain.CryptoType;
import com.ionutzbaur.crypto.investment.domain.CsvCrypto;
import com.ionutzbaur.crypto.investment.domain.StatisticType;
import com.ionutzbaur.crypto.investment.service.CryptoService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@RestController
public class CryptoController {

    private final CryptoService cryptoService;

    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @Operation(description = "Get a the requested statistic for a certain crypto (oldest/newest/min/max values)")
    @GetMapping("/stats/{crypto}")
    public CsvCrypto getStatisticValues(@PathVariable CryptoType crypto,
                                        @RequestParam(value = "type", defaultValue = "NEWEST") StatisticType statisticType) {
        return cryptoService.getStatistic(crypto, statisticType);
    }

    @Operation(description = "Get a descending sorted list of all the cryptos, " +
            "comparing the normalized range (max-min)/min")
    @GetMapping("/normalize-desc")
    public List<CsvCrypto> getNormalizedDesc() {
        return cryptoService.getNormalizedDesc();
    }

    @Operation(description = "Get the crypto symbol with the highest normalized range for a specific day")
    @GetMapping("/normalize-highest/{day}")
    public CryptoType getHighestNormalizedRange(@PathVariable String day) {
        LocalDate localDate;
        try {
            localDate = LocalDate.parse(day);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a valid date! Format should be YYYY-MM-DD");
        }

        return Optional
                .ofNullable(cryptoService.getHighestNormalizedRange(localDate))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No available data for the given day."));
    }

    @Operation(description = "Scale the service by adding new cryptos and/or adding data for more timeframes to existing cryptos. " +
            "Important note: New cryptos are not supported unless they are first added in the system.")
    @PutMapping("/crypto-values")
    public void addCrypto(@RequestBody List<CsvCrypto> cryptoValues) {
        cryptoService.addCrypto(cryptoValues);
    }

}
