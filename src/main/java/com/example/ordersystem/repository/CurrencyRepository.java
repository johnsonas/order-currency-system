package com.example.ordersystem.repository;

import com.example.ordersystem.model.Currency;
import com.example.ordersystem.model.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, CurrencyCode> {
    
    Optional<Currency> findByCurrencyCode(CurrencyCode currencyCode);
}


