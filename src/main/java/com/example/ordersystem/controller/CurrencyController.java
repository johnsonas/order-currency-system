package com.example.ordersystem.controller;

import com.example.ordersystem.model.Currency;
import com.example.ordersystem.service.CurrencyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/currencies")
@CrossOrigin(origins = "http://localhost:5173")
public class CurrencyController {
    
    @Autowired
    private CurrencyService currencyService;
    
    @GetMapping
    public ResponseEntity<List<Currency>> getAllCurrencies() {
        List<Currency> currencies = currencyService.getAllCurrencies();
        return ResponseEntity.ok(currencies);
    }
    
    @GetMapping("/{code}")
    public ResponseEntity<Currency> getCurrencyByCode(@PathVariable String code) {
        Optional<Currency> currency = currencyService.getCurrencyByCode(code);
        return currency.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Currency> createCurrency(@Valid @RequestBody Currency currency) {
        Currency createdCurrency = currencyService.createOrUpdateCurrency(currency);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCurrency);
    }
    
    @PutMapping("/{code}")
    public ResponseEntity<Currency> updateCurrency(
            @PathVariable String code,
            @Valid @RequestBody Currency currency) {
        currency.setCurrencyCode(code);
        Currency updatedCurrency = currencyService.createOrUpdateCurrency(currency);
        return ResponseEntity.ok(updatedCurrency);
    }
    
    @PutMapping("/{code}/rate")
    public ResponseEntity<Currency> updateRate(
            @PathVariable String code,
            @RequestBody BigDecimal newRate) {
        try {
            Currency updatedCurrency = currencyService.updateRate(code, newRate);
            return ResponseEntity.ok(updatedCurrency);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteCurrency(@PathVariable String code) {
        currencyService.deleteCurrency(code);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/convert")
    public ResponseEntity<BigDecimal> convertCurrency(
            @RequestParam BigDecimal amount,
            @RequestParam String sourceCurrency,
            @RequestParam String targetCurrency) {
        try {
            BigDecimal convertedAmount = currencyService.convertCurrency(amount, sourceCurrency, targetCurrency);
            return ResponseEntity.ok(convertedAmount);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

