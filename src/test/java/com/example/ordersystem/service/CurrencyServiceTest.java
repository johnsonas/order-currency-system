package com.example.ordersystem.service;

import com.example.ordersystem.model.Currency;
import com.example.ordersystem.model.CurrencyCode;
import com.example.ordersystem.repository.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CurrencyService 單元測試
 * 
 * @author Order Currency System
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CurrencyService 測試")
class CurrencyServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private CurrencyService currencyService;

    private Currency testCurrency;
    private CurrencyCode testCurrencyCode;

    @BeforeEach
    void setUp() {
        testCurrencyCode = CurrencyCode.USD;
        
        testCurrency = new Currency();
        testCurrency.setCurrencyCode(testCurrencyCode);
        testCurrency.setRateToTwd(new BigDecimal("31.250000"));
        testCurrency.setLastUpdate(LocalDateTime.now());
    }

    @Test
    @DisplayName("測試取得所有幣別 - 成功")
    void testGetAllCurrencies_Success() {
        // Arrange
        List<Currency> expectedCurrencies = Arrays.asList(testCurrency);
        when(currencyRepository.findAll()).thenReturn(expectedCurrencies);

        // Act
        List<Currency> result = currencyService.getAllCurrencies();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCurrencyCode, result.get(0).getCurrencyCode());
        verify(currencyRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("測試取得所有幣別 - 空列表")
    void testGetAllCurrencies_EmptyList() {
        // Arrange
        when(currencyRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Currency> result = currencyService.getAllCurrencies();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(currencyRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("測試取得所有幣別 - null 返回空列表")
    void testGetAllCurrencies_NullReturnsEmpty() {
        // Arrange
        when(currencyRepository.findAll()).thenReturn(null);

        // Act
        List<Currency> result = currencyService.getAllCurrencies();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(currencyRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("測試根據幣別代碼取得幣別 - 成功")
    void testGetCurrencyByCode_Success() {
        // Arrange
        when(currencyRepository.findByCurrencyCode(testCurrencyCode))
            .thenReturn(Optional.of(testCurrency));

        // Act
        Optional<Currency> result = currencyService.getCurrencyByCode(testCurrencyCode);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testCurrencyCode, result.get().getCurrencyCode());
        verify(currencyRepository, times(1)).findByCurrencyCode(testCurrencyCode);
    }

    @Test
    @DisplayName("測試根據幣別代碼取得幣別 - 不存在")
    void testGetCurrencyByCode_NotFound() {
        // Arrange
        when(currencyRepository.findByCurrencyCode(testCurrencyCode))
            .thenReturn(Optional.empty());

        // Act
        Optional<Currency> result = currencyService.getCurrencyByCode(testCurrencyCode);

        // Assert
        assertFalse(result.isPresent());
        verify(currencyRepository, times(1)).findByCurrencyCode(testCurrencyCode);
    }

    @Test
    @DisplayName("測試根據幣別代碼取得幣別 - null 參數")
    void testGetCurrencyByCode_NullParameter() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            currencyService.getCurrencyByCode(null);
        });
        verify(currencyRepository, never()).findByCurrencyCode(any());
    }

    @Test
    @DisplayName("測試建立或更新幣別 - 成功")
    void testCreateOrUpdateCurrency_Success() {
        // Arrange
        Currency newCurrency = new Currency();
        newCurrency.setCurrencyCode(CurrencyCode.EUR);
        newCurrency.setRateToTwd(new BigDecimal("34.480000"));

        when(currencyRepository.save(any(Currency.class))).thenReturn(newCurrency);

        // Act
        Currency result = currencyService.createOrUpdateCurrency(newCurrency);

        // Assert
        assertNotNull(result);
        assertEquals(CurrencyCode.EUR, result.getCurrencyCode());
        verify(currencyRepository, times(1)).save(newCurrency);
    }

    @Test
    @DisplayName("測試建立或更新幣別 - null 參數")
    void testCreateOrUpdateCurrency_NullParameter() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            currencyService.createOrUpdateCurrency(null);
        });
        verify(currencyRepository, never()).save(any());
    }

    @Test
    @DisplayName("測試刪除幣別 - 成功")
    void testDeleteCurrency_Success() {
        // Arrange
        doNothing().when(currencyRepository).deleteById(testCurrencyCode);

        // Act
        currencyService.deleteCurrency(testCurrencyCode);

        // Assert
        verify(currencyRepository, times(1)).deleteById(testCurrencyCode);
    }

    @Test
    @DisplayName("測試刪除幣別 - null 參數")
    void testDeleteCurrency_NullParameter() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            currencyService.deleteCurrency(null);
        });
        verify(currencyRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("測試轉換為TWD - TWD本身")
    void testConvertToTwd_TwdCurrency() {
        // Arrange
        BigDecimal amount = new BigDecimal("1000.00");

        // Act
        BigDecimal result = currencyService.convertToTwd(amount, CurrencyCode.TWD);

        // Assert
        assertNotNull(result);
        assertEquals(amount, result);
        verify(currencyRepository, never()).findByCurrencyCode(any());
    }

    @Test
    @DisplayName("測試轉換為TWD - 成功")
    void testConvertToTwd_Success() {
        // Arrange
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal expectedTwd = new BigDecimal("31250.00");
        when(currencyRepository.findByCurrencyCode(testCurrencyCode))
            .thenReturn(Optional.of(testCurrency));

        // Act
        BigDecimal result = currencyService.convertToTwd(amount, testCurrencyCode);

        // Assert
        assertNotNull(result);
        assertEquals(expectedTwd.setScale(2), result);
        verify(currencyRepository, times(1)).findByCurrencyCode(testCurrencyCode);
    }

    @Test
    @DisplayName("測試轉換為TWD - null 金額")
    void testConvertToTwd_NullAmount() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            currencyService.convertToTwd(null, testCurrencyCode);
        });
        verify(currencyRepository, never()).findByCurrencyCode(any());
    }

    @Test
    @DisplayName("測試轉換為TWD - 負數金額")
    void testConvertToTwd_NegativeAmount() {
        // Arrange
        BigDecimal negativeAmount = new BigDecimal("-100.00");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            currencyService.convertToTwd(negativeAmount, testCurrencyCode);
        });
        verify(currencyRepository, never()).findByCurrencyCode(any());
    }

    @Test
    @DisplayName("測試轉換為TWD - null 幣別")
    void testConvertToTwd_NullCurrency() {
        // Arrange
        BigDecimal amount = new BigDecimal("1000.00");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            currencyService.convertToTwd(amount, null);
        });
        verify(currencyRepository, never()).findByCurrencyCode(any());
    }

    @Test
    @DisplayName("測試轉換為TWD - 幣別不存在")
    void testConvertToTwd_CurrencyNotFound() {
        // Arrange
        BigDecimal amount = new BigDecimal("1000.00");
        when(currencyRepository.findByCurrencyCode(testCurrencyCode))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            currencyService.convertToTwd(amount, testCurrencyCode);
        });
        verify(currencyRepository, times(1)).findByCurrencyCode(testCurrencyCode);
    }

    @Test
    @DisplayName("測試轉換為TWD - 匯率為null")
    void testConvertToTwd_NullRate() {
        // Arrange
        BigDecimal amount = new BigDecimal("1000.00");
        Currency currencyWithNullRate = new Currency();
        currencyWithNullRate.setCurrencyCode(testCurrencyCode);
        currencyWithNullRate.setRateToTwd(null);
        
        when(currencyRepository.findByCurrencyCode(testCurrencyCode))
            .thenReturn(Optional.of(currencyWithNullRate));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            currencyService.convertToTwd(amount, testCurrencyCode);
        });
        verify(currencyRepository, times(1)).findByCurrencyCode(testCurrencyCode);
    }

    @Test
    @DisplayName("測試幣別轉換 - 相同幣別")
    void testConvertCurrency_SameCurrency() {
        // Arrange
        BigDecimal amount = new BigDecimal("1000.00");

        // Act
        BigDecimal result = currencyService.convertCurrency(
            amount, testCurrencyCode, testCurrencyCode);

        // Assert
        assertNotNull(result);
        assertEquals(amount, result);
        verify(currencyRepository, never()).findByCurrencyCode(any());
    }

    @Test
    @DisplayName("測試幣別轉換 - 轉換為TWD")
    void testConvertCurrency_ToTwd() {
        // Arrange
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal expectedTwd = new BigDecimal("31250.00");
        when(currencyRepository.findByCurrencyCode(testCurrencyCode))
            .thenReturn(Optional.of(testCurrency));

        // Act
        BigDecimal result = currencyService.convertCurrency(
            amount, testCurrencyCode, CurrencyCode.TWD);

        // Assert
        assertNotNull(result);
        assertEquals(expectedTwd.setScale(2), result);
        verify(currencyRepository, times(1)).findByCurrencyCode(testCurrencyCode);
    }

    @Test
    @DisplayName("測試幣別轉換 - 從TWD轉換")
    void testConvertCurrency_FromTwd() {
        // Arrange
        BigDecimal amount = new BigDecimal("31250.00");
        BigDecimal expectedUsd = new BigDecimal("1000.00");
        when(currencyRepository.findByCurrencyCode(testCurrencyCode))
            .thenReturn(Optional.of(testCurrency));

        // Act
        BigDecimal result = currencyService.convertCurrency(
            amount, CurrencyCode.TWD, testCurrencyCode);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUsd.setScale(2), result);
        verify(currencyRepository, times(1)).findByCurrencyCode(testCurrencyCode);
    }

    @Test
    @DisplayName("測試幣別轉換 - 成功")
    void testConvertCurrency_Success() {
        // Arrange
        BigDecimal amount = new BigDecimal("1000.00");
        CurrencyCode sourceCurrency = CurrencyCode.USD;
        CurrencyCode targetCurrency = CurrencyCode.EUR;
        
        Currency eurCurrency = new Currency();
        eurCurrency.setCurrencyCode(CurrencyCode.EUR);
        eurCurrency.setRateToTwd(new BigDecimal("34.480000"));

        when(currencyRepository.findByCurrencyCode(sourceCurrency))
            .thenReturn(Optional.of(testCurrency));
        when(currencyRepository.findByCurrencyCode(targetCurrency))
            .thenReturn(Optional.of(eurCurrency));

        // Act
        BigDecimal result = currencyService.convertCurrency(
            amount, sourceCurrency, targetCurrency);

        // Assert
        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
        verify(currencyRepository, times(1)).findByCurrencyCode(sourceCurrency);
        verify(currencyRepository, times(1)).findByCurrencyCode(targetCurrency);
    }

    @Test
    @DisplayName("測試幣別轉換 - null 金額")
    void testConvertCurrency_NullAmount() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            currencyService.convertCurrency(null, testCurrencyCode, CurrencyCode.EUR);
        });
        verify(currencyRepository, never()).findByCurrencyCode(any());
    }

    @Test
    @DisplayName("測試幣別轉換 - null 來源幣別")
    void testConvertCurrency_NullSourceCurrency() {
        // Arrange
        BigDecimal amount = new BigDecimal("1000.00");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            currencyService.convertCurrency(amount, null, CurrencyCode.EUR);
        });
        verify(currencyRepository, never()).findByCurrencyCode(any());
    }

    @Test
    @DisplayName("測試幣別轉換 - null 目標幣別")
    void testConvertCurrency_NullTargetCurrency() {
        // Arrange
        BigDecimal amount = new BigDecimal("1000.00");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            currencyService.convertCurrency(amount, testCurrencyCode, null);
        });
        verify(currencyRepository, never()).findByCurrencyCode(any());
    }

    @Test
    @DisplayName("測試幣別轉換 - 匯率為0")
    void testConvertCurrency_ZeroRate() {
        // Arrange
        BigDecimal amount = new BigDecimal("1000.00");
        Currency currencyWithZeroRate = new Currency();
        currencyWithZeroRate.setCurrencyCode(CurrencyCode.EUR);
        currencyWithZeroRate.setRateToTwd(BigDecimal.ZERO);

        when(currencyRepository.findByCurrencyCode(testCurrencyCode))
            .thenReturn(Optional.of(testCurrency));
        when(currencyRepository.findByCurrencyCode(CurrencyCode.EUR))
            .thenReturn(Optional.of(currencyWithZeroRate));

        // Act & Assert
        assertThrows(ArithmeticException.class, () -> {
            currencyService.convertCurrency(amount, testCurrencyCode, CurrencyCode.EUR);
        });
    }

    @Test
    @DisplayName("測試更新匯率 - 成功")
    void testUpdateRate_Success() {
        // Arrange
        BigDecimal newRate = new BigDecimal("32.000000");
        testCurrency.setRateToTwd(newRate);
        when(currencyRepository.findByCurrencyCode(testCurrencyCode))
            .thenReturn(Optional.of(testCurrency));
        when(currencyRepository.save(any(Currency.class))).thenReturn(testCurrency);

        // Act
        Currency result = currencyService.updateRate(testCurrencyCode, newRate);

        // Assert
        assertNotNull(result);
        assertEquals(newRate, result.getRateToTwd());
        verify(currencyRepository, times(1)).findByCurrencyCode(testCurrencyCode);
        verify(currencyRepository, times(1)).save(any(Currency.class));
    }

    @Test
    @DisplayName("測試更新匯率 - null 幣別代碼")
    void testUpdateRate_NullCurrencyCode() {
        // Arrange
        BigDecimal newRate = new BigDecimal("32.000000");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            currencyService.updateRate(null, newRate);
        });
        verify(currencyRepository, never()).findByCurrencyCode(any());
    }

    @Test
    @DisplayName("測試更新匯率 - null 匯率")
    void testUpdateRate_NullRate() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            currencyService.updateRate(testCurrencyCode, null);
        });
        verify(currencyRepository, never()).findByCurrencyCode(any());
    }

    @Test
    @DisplayName("測試更新匯率 - 匯率為0")
    void testUpdateRate_ZeroRate() {
        // Arrange
        BigDecimal zeroRate = BigDecimal.ZERO;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            currencyService.updateRate(testCurrencyCode, zeroRate);
        });
        verify(currencyRepository, never()).findByCurrencyCode(any());
    }

    @Test
    @DisplayName("測試更新匯率 - 負數匯率")
    void testUpdateRate_NegativeRate() {
        // Arrange
        BigDecimal negativeRate = new BigDecimal("-10.00");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            currencyService.updateRate(testCurrencyCode, negativeRate);
        });
        verify(currencyRepository, never()).findByCurrencyCode(any());
    }

    @Test
    @DisplayName("測試更新匯率 - 幣別不存在")
    void testUpdateRate_CurrencyNotFound() {
        // Arrange
        BigDecimal newRate = new BigDecimal("32.000000");
        when(currencyRepository.findByCurrencyCode(testCurrencyCode))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            currencyService.updateRate(testCurrencyCode, newRate);
        });
        verify(currencyRepository, times(1)).findByCurrencyCode(testCurrencyCode);
        verify(currencyRepository, never()).save(any());
    }
}

