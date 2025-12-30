package com.example.ordersystem.integration;

import com.example.ordersystem.model.CurrencyCode;
import com.example.ordersystem.model.Order;
import com.example.ordersystem.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OrderController.getAllOrders() 整合測試 - 使用真實的 Oracle 資料庫
 * 
 * 這才是真正的整合測試，因為：
 * 1. 使用與生產環境相同的 Oracle 資料庫
 * 2. 可以發現 Oracle 特定的 SQL 語法問題
 * 3. 測試結果更接近真實環境
 * 
 * 注意：這個測試需要 Oracle 資料庫正在運行
 * 
 * @author Order Currency System
 * @version 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-oracle")  // 使用 Oracle 資料庫配置
// 注意：不使用 @Transactional，這樣測試後可以在資料庫中看到資料
@DisplayName("OrderController.getAllOrders() 整合測試 - Oracle 資料庫")
class OrderControllerGetAllOrdersOracleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    private Order testOrder;

    @BeforeEach
    void setUp() {

        
        // 建立測試訂單並存入 Oracle 資料庫
        testOrder = new Order();
        testOrder.setUsername("testuser_" + System.currentTimeMillis());  // 使用時間戳記避免重複
        testOrder.setAmount(new BigDecimal("1000.00"));
        testOrder.setCurrency(CurrencyCode.USD);
        testOrder.setStatus("PENDING");
        testOrder.setDiscount(new BigDecimal("10.00"));
        testOrder.setFinalAmount(new BigDecimal("900.00"));
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());
        
        // 存入 Oracle 資料庫（真實的資料庫操作）
        testOrder = orderRepository.save(testOrder);
        
        System.out.println("\n========== 已存入 Oracle 資料庫 ==========");
        System.out.println("訂單ID: " + testOrder.getOrderId());
        System.out.println("使用者名稱: " + testOrder.getUsername());
        System.out.println("金額: " + testOrder.getAmount());
        System.out.println("注意：不會刪除資料庫中現有的資料");
        System.out.println("==========================================\n");
    }

    @Test
    @DisplayName("整合測試 - 取得所有訂單 - 成功（使用 Oracle 資料庫）")
    void testGetAllOrders_Success() throws Exception {
        // Arrange
        // 測試資料已經在 setUp() 中存入 Oracle 資料庫
        // 這裡使用的是真實的 Oracle 資料庫，不是 H2

        // 從 Oracle 資料庫直接查詢資料並印出
        System.out.println("\n========== 從 Oracle 資料庫直接查詢的資料 ==========");
        List<Order> ordersFromDb = orderRepository.findAll();
        System.out.println("Oracle 資料庫中的訂單數量: " + ordersFromDb.size());
        for (Order order : ordersFromDb) {
            System.out.println("訂單ID: " + order.getOrderId());
            System.out.println("使用者名稱: " + order.getUsername());
            System.out.println("金額: " + order.getAmount());
            System.out.println("幣別: " + order.getCurrency());
            System.out.println("狀態: " + order.getStatus());
            System.out.println("折扣: " + order.getDiscount());
            System.out.println("最終金額: " + order.getFinalAmount());
            System.out.println("建立時間: " + order.getCreatedAt());
            System.out.println("更新時間: " + order.getUpdatedAt());
            System.out.println("----------------------------------------");
        }

        // Act & Assert
        String responseContent = mockMvc.perform(get("/api/orders"))
            .andExpect(status().isOk())  // 驗證：HTTP 狀態碼應該是 200 OK
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))  // 驗證：回應內容類型應該是 JSON
            .andExpect(jsonPath("$[0].orderId").value(testOrder.getOrderId()))  // 驗證：JSON 陣列第一個元素的 orderId
            .andExpect(jsonPath("$[0].username").value(testOrder.getUsername()))  // 驗證：username
            .andExpect(jsonPath("$[0].amount").value(1000.00))  // 驗證：amount 應該是 1000.00
            .andExpect(jsonPath("$[0].currency").value("USD"))  // 驗證：currency 應該是 "USD"
            .andExpect(jsonPath("$[0].status").value("PENDING"))  // 驗證：status 應該是 "PENDING"
            .andExpect(jsonPath("$[0].discount").value(10.00))  // 驗證：discount 應該是 10.00
            .andExpect(jsonPath("$[0].finalAmount").value(900.00))  // 驗證：finalAmount 應該是 900.00
            .andReturn()
            .getResponse()
            .getContentAsString();

        // 印出 API 回應的 JSON 資料
        System.out.println("\n========== API 回應的 JSON 資料 ==========");
        System.out.println(responseContent);
        System.out.println("==========================================\n");
        
        System.out.println("✅ 測試完成！資料已存入 Oracle 資料庫，你可以在資料庫中查看。");
    }
}

