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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OrderController.getAllOrders() 整合測試 - 使用 H2 記憶體資料庫
 * 
 * 注意：這個測試使用 H2 記憶體資料庫，不是 Oracle
 * - 優點：測試速度快，不需要外部依賴
 * - 缺點：無法發現 Oracle 特定的 SQL 語法問題
 * 
 * 如果需要測試 Oracle 資料庫，請使用：
 * OrderControllerGetAllOrdersOracleIntegrationTest
 * 
 * @author Order Currency System
 * @version 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")  // 使用 H2 記憶體資料庫
@Transactional  // 測試後自動回滾
@DisplayName("OrderController.getAllOrders() 整合測試 - H2 資料庫")
class OrderControllerGetAllOrdersIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        // 注意：H2 記憶體資料庫，測試結束後會自動清理
        // 這裡可以刪除，因為是記憶體資料庫，不會影響真實資料
        
        // 清理資料（確保測試環境乾淨）
        orderRepository.deleteAll();

        // 建立測試訂單並存入資料庫
        testOrder = new Order();
        testOrder.setUsername("testuser");
        testOrder.setAmount(new BigDecimal("1000.00"));
        testOrder.setCurrency(CurrencyCode.USD);
        testOrder.setStatus("PENDING");
        testOrder.setDiscount(new BigDecimal("10.00"));
        testOrder.setFinalAmount(new BigDecimal("900.00"));
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());
        
        // 存入資料庫（真實的資料庫操作）
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    @DisplayName("整合測試 - 取得所有訂單 - 成功")
    void testGetAllOrders_Success() throws Exception {
        // Arrange
        // 測試資料已經在 setUp() 中存入資料庫
        // 這裡不需要 Mock，因為會使用真實的 Service 和 Repository

        // 從資料庫直接查詢資料並印出
        System.out.println("\n========== 從資料庫直接查詢的資料 ==========");
        List<Order> ordersFromDb = orderRepository.findAll();
        System.out.println("資料庫中的訂單數量: " + ordersFromDb.size());
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
            .andExpect(jsonPath("$[0].username").value("testuser"))  // 驗證：username 應該是 "testuser"
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

        // 注意：整合測試不需要 verify()，因為使用的是真實的 Service
        // 如果要用 verify，需要注入 Service 並使用 @SpyBean
    }

    @Test
    @DisplayName("整合測試 - 取得所有訂單 - 多筆資料")
    void testGetAllOrders_MultipleOrders() throws Exception {
        // Arrange: 建立多筆訂單
        Order order1 = new Order();
        order1.setUsername("user1");
        order1.setAmount(new BigDecimal("1000.00"));
        order1.setCurrency(CurrencyCode.USD);
        order1.setStatus("PENDING");
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setUsername("user2");
        order2.setAmount(new BigDecimal("2000.00"));
        order2.setCurrency(CurrencyCode.EUR);
        order2.setStatus("CONFIRMED");
        orderRepository.save(order2);

        Order order3 = new Order();
        order3.setUsername("user3");
        order3.setAmount(new BigDecimal("3000.00"));
        order3.setCurrency(CurrencyCode.JPY);
        order3.setStatus("COMPLETED");
        orderRepository.save(order3);

        // Act & Assert
        mockMvc.perform(get("/api/orders"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(4))  // 包含 setUp() 中的 testOrder，共 4 筆
            .andExpect(jsonPath("$[0].username").exists())  // 驗證第一筆有 username
            .andExpect(jsonPath("$[1].username").exists())  // 驗證第二筆有 username
            .andExpect(jsonPath("$[2].username").exists())  // 驗證第三筆有 username
            .andExpect(jsonPath("$[3].username").exists());  // 驗證第四筆有 username
    }

    @Test
    @DisplayName("整合測試 - 取得所有訂單 - 空列表")
    void testGetAllOrders_EmptyList() throws Exception {
        // Arrange: 清理所有資料
        orderRepository.deleteAll();

        // Act & Assert
        mockMvc.perform(get("/api/orders"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(0));  // 驗證：應該是空陣列
    }

    @Test
    @DisplayName("整合測試 - 取得所有訂單 - 驗證排序（按建立時間降序）")
    void testGetAllOrders_SortedByCreatedAt() throws Exception {
        // Arrange: 建立多筆訂單，並設定不同的建立時間
        orderRepository.deleteAll();

        Order order1 = new Order();
        order1.setUsername("user1");
        order1.setAmount(new BigDecimal("1000.00"));
        order1.setCurrency(CurrencyCode.USD);
        order1.setCreatedAt(LocalDateTime.now().minusHours(3));  // 3 小時前
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setUsername("user2");
        order2.setAmount(new BigDecimal("2000.00"));
        order2.setCurrency(CurrencyCode.EUR);
        order2.setCreatedAt(LocalDateTime.now().minusHours(1));  // 1 小時前
        orderRepository.save(order2);

        Order order3 = new Order();
        order3.setUsername("user3");
        order3.setAmount(new BigDecimal("3000.00"));
        order3.setCurrency(CurrencyCode.JPY);
        order3.setCreatedAt(LocalDateTime.now());  // 現在
        orderRepository.save(order3);

        // Act & Assert
        // 驗證：最新的訂單應該在第一個位置（按建立時間降序）
        mockMvc.perform(get("/api/orders"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].username").value("user3"))  // 最新的應該在第一個
            .andExpect(jsonPath("$[1].username").value("user2"))  // 第二新的
            .andExpect(jsonPath("$[2].username").value("user1"));  // 最舊的
    }
}

