package com.example.ordersystem.integration;

import com.example.ordersystem.model.CurrencyCode;
import com.example.ordersystem.model.Order;
import com.example.ordersystem.repository.CurrencyRepository;
import com.example.ordersystem.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OrderController 整合測試
 * 使用真實的資料庫連線進行測試
 * 
 * @author Order Currency System
 * @version 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("OrderController 整合測試")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 清理資料
        orderRepository.deleteAll();
        currencyRepository.deleteAll();

        // 初始化測試幣別資料
        // 注意：這裡需要根據實際的 Currency 實體來建立
    }

    @Test
    @DisplayName("整合測試 - 建立並查詢訂單")
    void testCreateAndGetOrder() throws Exception {
        // Arrange: 建立訂單請求
        Order newOrder = new Order();
        newOrder.setUsername("integrationtest");
        newOrder.setAmount(new BigDecimal("1000.00"));
        newOrder.setCurrency(CurrencyCode.USD);
        newOrder.setStatus("PENDING");
        newOrder.setDiscount(new BigDecimal("10.00"));

        // Act: 建立訂單
        String response = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newOrder)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.username").value("integrationtest"))
            .andExpect(jsonPath("$.finalAmount").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // 解析回應取得訂單ID
        Order createdOrder = objectMapper.readValue(response, Order.class);
        Long orderId = createdOrder.getOrderId();

        // Assert: 查詢訂單
        mockMvc.perform(get("/api/orders/" + orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(orderId))
            .andExpect(jsonPath("$.username").value("integrationtest"))
            .andExpect(jsonPath("$.finalAmount").value(900.00));
    }

    @Test
    @DisplayName("整合測試 - 更新訂單")
    void testUpdateOrder() throws Exception {
        // Arrange: 先建立一個訂單
        Order order = new Order();
        order.setUsername("testuser");
        order.setAmount(new BigDecimal("1000.00"));
        order.setCurrency(CurrencyCode.USD);
        order.setStatus("PENDING");
        order.setDiscount(new BigDecimal("10.00"));
        Order savedOrder = orderRepository.save(order);
        Long orderId = savedOrder.getOrderId();

        // Arrange: 準備更新資料
        Order updateData = new Order();
        updateData.setUsername("updateduser");
        updateData.setAmount(new BigDecimal("2000.00"));
        updateData.setCurrency(CurrencyCode.EUR);
        updateData.setStatus("CONFIRMED");
        updateData.setDiscount(new BigDecimal("15.00"));

        // Act & Assert: 更新訂單
        mockMvc.perform(put("/api/orders/" + orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("updateduser"))
            .andExpect(jsonPath("$.status").value("CONFIRMED"))
            .andExpect(jsonPath("$.finalAmount").value(1700.00));
    }

    @Test
    @DisplayName("整合測試 - 刪除訂單")
    void testDeleteOrder() throws Exception {
        // Arrange: 先建立一個訂單
        Order order = new Order();
        order.setUsername("testuser");
        order.setAmount(new BigDecimal("1000.00"));
        order.setCurrency(CurrencyCode.USD);
        order.setStatus("PENDING");
        order.setDiscount(new BigDecimal("10.00"));
        Order savedOrder = orderRepository.save(order);
        Long orderId = savedOrder.getOrderId();

        // Act & Assert: 刪除訂單
        mockMvc.perform(delete("/api/orders/" + orderId))
            .andExpect(status().isNoContent());

        // Assert: 確認訂單已刪除
        mockMvc.perform(get("/api/orders/" + orderId))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("整合測試 - 取得所有訂單")
    void testGetAllOrders() throws Exception {
        // Arrange: 建立多筆訂單
        Order order1 = new Order();
        order1.setUsername("user1");
        order1.setAmount(new BigDecimal("1000.00"));
        order1.setCurrency(CurrencyCode.USD);
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setUsername("user2");
        order2.setAmount(new BigDecimal("2000.00"));
        order2.setCurrency(CurrencyCode.EUR);
        orderRepository.save(order2);

        // Act & Assert
        mockMvc.perform(get("/api/orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }
}



