package com.example.ordersystem.service;

import com.example.ordersystem.model.CurrencyCode;
import com.example.ordersystem.model.Order;
import com.example.ordersystem.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * OrderService 單元測試
 * 
 * @author Order Currency System
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 測試")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private CurrencyCode testCurrency;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setOrderId(1L);
        testOrder.setUsername("testuser");
        testOrder.setAmount(new BigDecimal("1000.00"));
        testOrder.setCurrency(CurrencyCode.USD);
        testOrder.setStatus("PENDING");
        testOrder.setDiscount(new BigDecimal("10.00"));
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());

        testCurrency = CurrencyCode.USD;
    }

    @Test
    @DisplayName("測試取得所有訂單 - 成功")
    void testGetAllOrders_Success() {
        // Arrange
        List<Order> expectedOrders = Arrays.asList(testOrder);
        when(orderRepository.findAll(any(Sort.class))).thenReturn(expectedOrders);

        // Act
        List<Order> result = orderService.getAllOrders();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder.getOrderId(), result.get(0).getOrderId());
        verify(orderRepository, times(1)).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("測試根據ID取得訂單 - 成功")
    void testGetOrderById_Success() {
        // Arrange
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // Act
        Optional<Order> result = orderService.getOrderById(orderId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testOrder.getOrderId(), result.get().getOrderId());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("測試根據ID取得訂單 - 不存在")
    void testGetOrderById_NotFound() {
        // Arrange
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act
        Optional<Order> result = orderService.getOrderById(orderId);

        // Assert
        assertFalse(result.isPresent());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("測試根據使用者名稱取得訂單 - 成功")
    void testGetOrdersByUsername_Success() {
        // Arrange
        String username = "testuser";
        List<Order> expectedOrders = Arrays.asList(testOrder);
        when(orderRepository.findByUsername(username)).thenReturn(expectedOrders);

        // Act
        List<Order> result = orderService.getOrdersByUsername(username);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(username, result.get(0).getUsername());
        verify(orderRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("測試根據狀態取得訂單 - 成功")
    void testGetOrdersByStatus_Success() {
        // Arrange
        String status = "PENDING";
        List<Order> expectedOrders = Arrays.asList(testOrder);
        when(orderRepository.findByStatus(status)).thenReturn(expectedOrders);

        // Act
        List<Order> result = orderService.getOrdersByStatus(status);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(status, result.get(0).getStatus());
        verify(orderRepository, times(1)).findByStatus(status);
    }

    @Test
    @DisplayName("測試搜尋訂單ID - 精確匹配")
    void testSearchOrdersByOrderId_ExactMatch() {
        // Arrange
        String searchId = "1";
        when(orderRepository.findByOrderId(1L)).thenReturn(Optional.of(testOrder));

        // Act
        List<Order> result = orderService.searchOrdersByOrderId(searchId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder.getOrderId(), result.get(0).getOrderId());
        verify(orderRepository, times(1)).findByOrderId(1L);
        verify(orderRepository, never()).searchByOrderIdContaining(anyString());
    }

    @Test
    @DisplayName("測試搜尋訂單ID - 模糊搜尋")
    void testSearchOrdersByOrderId_FuzzySearch() {
        // Arrange
        String searchId = "12";
        List<Order> expectedOrders = Arrays.asList(testOrder);
        when(orderRepository.findByOrderId(anyLong())).thenReturn(Optional.empty());
        when(orderRepository.searchByOrderIdContaining(searchId)).thenReturn(expectedOrders);

        // Act
        List<Order> result = orderService.searchOrdersByOrderId(searchId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).searchByOrderIdContaining(searchId);
    }

    @Test
    @DisplayName("測試搜尋訂單ID - 空字串返回所有訂單")
    void testSearchOrdersByOrderId_EmptyString() {
        // Arrange
        String searchId = "";
        List<Order> expectedOrders = Arrays.asList(testOrder);
        when(orderRepository.findAll(any(Sort.class))).thenReturn(expectedOrders);

        // Act
        List<Order> result = orderService.searchOrdersByOrderId(searchId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("測試建立訂單 - 成功")
    void testCreateOrder_Success() {
        // Arrange
        Order newOrder = new Order();
        newOrder.setUsername("newuser");
        newOrder.setAmount(new BigDecimal("500.00"));
        newOrder.setCurrency(CurrencyCode.TWD);
        newOrder.setDiscount(new BigDecimal("5.00"));

        Order savedOrder = new Order();
        savedOrder.setOrderId(2L);
        savedOrder.setUsername(newOrder.getUsername());
        savedOrder.setAmount(newOrder.getAmount());
        savedOrder.setCurrency(newOrder.getCurrency());
        savedOrder.setDiscount(newOrder.getDiscount());
        savedOrder.setFinalAmount(new BigDecimal("475.00"));

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        Order result = orderService.createOrder(newOrder);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getFinalAmount());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("測試更新訂單 - 成功")
    void testUpdateOrder_Success() {
        // Arrange
        Long orderId = 1L;
        Order updateData = new Order();
        updateData.setUsername("updateduser");
        updateData.setAmount(new BigDecimal("2000.00"));
        updateData.setCurrency(CurrencyCode.EUR);
        updateData.setStatus("CONFIRMED");
        updateData.setDiscount(new BigDecimal("15.00"));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        Order result = orderService.updateOrder(orderId, updateData);

        // Assert
        assertNotNull(result);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("測試更新訂單 - 訂單不存在")
    void testUpdateOrder_NotFound() {
        // Arrange
        Long orderId = 999L;
        Order updateData = new Order();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.updateOrder(orderId, updateData);
        });
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("測試刪除訂單 - 成功")
    void testDeleteOrder_Success() {
        // Arrange
        Long orderId = 1L;
        doNothing().when(orderRepository).deleteById(orderId);

        // Act
        orderService.deleteOrder(orderId);

        // Assert
        verify(orderRepository, times(1)).deleteById(orderId);
    }

    @Test
    @DisplayName("測試轉換為TWD - 成功")
    void testConvertToTwd_Success() {
        // Arrange
        Long orderId = 1L;
        BigDecimal expectedTwdAmount = new BigDecimal("31250.00");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(currencyService.convertToTwd(
            eq(testOrder.getFinalAmount()),
            eq(testOrder.getCurrency())
        )).thenReturn(expectedTwdAmount);

        // Act
        BigDecimal result = orderService.convertToTwd(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedTwdAmount, result);
        verify(orderRepository, times(1)).findById(orderId);
        verify(currencyService, times(1)).convertToTwd(
            eq(testOrder.getFinalAmount()),
            eq(testOrder.getCurrency())
        );
    }

    @Test
    @DisplayName("測試轉換為TWD - 訂單不存在")
    void testConvertToTwd_NotFound() {
        // Arrange
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.convertToTwd(orderId);
        });
        verify(orderRepository, times(1)).findById(orderId);
        verify(currencyService, never()).convertToTwd(any(), any());
    }

    @Test
    @DisplayName("測試轉換幣別 - 成功")
    void testConvertCurrency_Success() {
        // Arrange
        Long orderId = 1L;
        CurrencyCode targetCurrency = CurrencyCode.EUR;
        BigDecimal expectedAmount = new BigDecimal("906.25");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(currencyService.convertCurrency(
            eq(testOrder.getFinalAmount()),
            eq(testOrder.getCurrency()),
            eq(targetCurrency)
        )).thenReturn(expectedAmount);

        // Act
        BigDecimal result = orderService.convertCurrency(orderId, targetCurrency);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAmount, result);
        verify(orderRepository, times(1)).findById(orderId);
        verify(currencyService, times(1)).convertCurrency(
            eq(testOrder.getFinalAmount()),
            eq(testOrder.getCurrency()),
            eq(targetCurrency)
        );
    }

    @Test
    @DisplayName("測試轉換幣別 - 訂單不存在")
    void testConvertCurrency_NotFound() {
        // Arrange
        Long orderId = 999L;
        CurrencyCode targetCurrency = CurrencyCode.EUR;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.convertCurrency(orderId, targetCurrency);
        });
        verify(orderRepository, times(1)).findById(orderId);
        verify(currencyService, never()).convertCurrency(any(), any(), any());
    }
}



