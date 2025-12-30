package com.example.ordersystem.controller;

import com.example.ordersystem.model.CurrencyCode;
import com.example.ordersystem.model.Order;
import com.example.ordersystem.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OrderController 測試
 * 使用 MockMvc 測試 REST API 端點
 * 
 * @author Order Currency System
 * @version 1.0
 */
@WebMvcTest(OrderController.class)
@DisplayName("OrderController 測試")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setOrderId(1L);
        testOrder.setUsername("testuser");
        testOrder.setAmount(new BigDecimal("1000.00"));
        testOrder.setCurrency(CurrencyCode.USD);
        testOrder.setStatus("PENDING");
        testOrder.setDiscount(new BigDecimal("10.00"));
        testOrder.setFinalAmount(new BigDecimal("900.00"));
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("測試取得所有訂單 - 成功")
    void testGetAllOrders_Success() throws Exception {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);  // 建立測試資料：包含一個測試訂單的列表
        when(orderService.getAllOrders()).thenReturn(orders);  // 模擬 OrderService：當呼叫 getAllOrders() 時，返回測試資料
        
        // Act & Assert (執行與驗證)
        mockMvc.perform(get("/api/orders"))  // 模擬發送 GET 請求到 /api/orders
            .andExpect(status().isOk())  // 驗證：HTTP 狀態碼應該是 200 OK
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))  // 驗證：回應內容類型應該是 JSON
            .andExpect(jsonPath("$[0].orderId").value(1))  // 驗證：JSON 陣列第一個元素的 orderId 應該是 1
            .andExpect(jsonPath("$[0].username").value("testuser"))  // 驗證：username 應該是 "testuser"
            .andExpect(jsonPath("$[0].amount").value(1000.00))  // 驗證：amount 應該是 1000.00
            .andExpect(jsonPath("$[0].currency").value("USD"));  // 驗證：currency 應該是 "USD"
        
        verify(orderService, times(1)).getAllOrders();  // 驗證：OrderService.getAllOrders() 應該被呼叫 1 次
    }

    @Test
    @DisplayName("測試根據ID取得訂單 - 成功")
    void testGetOrderById_Success() throws Exception {
        // Arrange
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        mockMvc.perform(get("/api/orders/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.orderId").value(1))
            .andExpect(jsonPath("$.username").value("testuser"));

        verify(orderService, times(1)).getOrderById(1L);
    }

    @Test
    @DisplayName("測試根據ID取得訂單 - 不存在")
    void testGetOrderById_NotFound() throws Exception {
        // Arrange
        when(orderService.getOrderById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/orders/999"))
            .andExpect(status().isNotFound());

        verify(orderService, times(1)).getOrderById(999L);
    }

    @Test
    @DisplayName("測試建立訂單 - 成功")
    void testCreateOrder_Success() throws Exception {
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

        when(orderService.createOrder(any(Order.class))).thenReturn(savedOrder);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newOrder)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.orderId").value(2))
            .andExpect(jsonPath("$.username").value("newuser"));

        verify(orderService, times(1)).createOrder(any(Order.class));
    }

    @Test
    @DisplayName("測試更新訂單 - 成功")
    void testUpdateOrder_Success() throws Exception {
        // Arrange
        Order updateData = new Order();
        updateData.setUsername("updateduser");
        updateData.setAmount(new BigDecimal("2000.00"));
        updateData.setCurrency(CurrencyCode.EUR);
        updateData.setStatus("CONFIRMED");
        updateData.setDiscount(new BigDecimal("15.00"));

        testOrder.setUsername(updateData.getUsername());
        testOrder.setAmount(updateData.getAmount());
        testOrder.setCurrency(updateData.getCurrency());
        testOrder.setStatus(updateData.getStatus());
        testOrder.setDiscount(updateData.getDiscount());

        when(orderService.updateOrder(eq(1L), any(Order.class))).thenReturn(testOrder);

        // Act & Assert
        mockMvc.perform(put("/api/orders/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.username").value("updateduser"))
            .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(orderService, times(1)).updateOrder(eq(1L), any(Order.class));
    }

    @Test
    @DisplayName("測試刪除訂單 - 成功")
    void testDeleteOrder_Success() throws Exception {
        // Arrange
        doNothing().when(orderService).deleteOrder(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/orders/1"))
            .andExpect(status().isNoContent());

        verify(orderService, times(1)).deleteOrder(1L);
    }

    @Test
    @DisplayName("測試轉換為TWD - 成功")
    void testConvertToTwd_Success() throws Exception {
        // Arrange
        BigDecimal expectedTwd = new BigDecimal("31250.00");
        when(orderService.convertToTwd(1L)).thenReturn(expectedTwd);

        // Act & Assert
        mockMvc.perform(get("/api/orders/1/convert/twd"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").value(31250.00));

        verify(orderService, times(1)).convertToTwd(1L);
    }

    @Test
    @DisplayName("測試搜尋訂單 - 成功")
    void testSearchOrders_Success() throws Exception {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderService.searchOrdersByOrderId("1")).thenReturn(orders);

        // Act & Assert
        mockMvc.perform(get("/api/orders").param("searchOrderId", "1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].orderId").value(1));

        verify(orderService, times(1)).searchOrdersByOrderId("1");
    }
}



