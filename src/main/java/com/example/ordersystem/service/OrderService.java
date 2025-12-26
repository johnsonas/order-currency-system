package com.example.ordersystem.service;

import com.example.ordersystem.model.Order;
import com.example.ordersystem.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * 訂單服務類
 * 提供訂單相關的業務邏輯處理，包括訂單的 CRUD 操作、搜尋、幣別轉換等功能
 * 
 * @author Order Currency System
 * @version 1.0
 */
@Service
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CurrencyService currencyService;
    
    /**
     * 取得所有訂單列表
     * 按照建立時間降序排列（最新的訂單在前）
     * 
     * @return 所有訂單的列表，按建立時間降序排列
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
    
    /**
     * 根據訂單ID取得訂單
     * 
     * @param orderId 訂單ID
     * @return 訂單的 Optional 物件，如果不存在則返回空 Optional
     */
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }
    
    /**
     * 根據使用者名稱取得訂單列表
     * 
     * @param username 使用者名稱
     * @return 該使用者的所有訂單列表
     */
    public List<Order> getOrdersByUsername(String username) {
        return orderRepository.findByUsername(username);
    }
    
    /**
     * 根據訂單狀態取得訂單列表
     * 
     * @param status 訂單狀態（如：PENDING, CONFIRMED, CANCELLED, COMPLETED）
     * @return 符合該狀態的所有訂單列表
     */
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }
    
    /**
     * 根據訂單ID進行搜尋
     * 支援精確匹配和模糊搜尋：
     * - 如果輸入的是完整數字，先嘗試精確匹配
     * - 如果精確匹配失敗或輸入不是數字，則進行模糊搜尋（部分匹配）
     * - 如果搜尋條件為空，返回所有訂單
     * 
     * @param orderId 訂單ID（可以是完整ID或部分ID字串）
     * @return 符合搜尋條件的訂單列表，按建立時間降序排列
     */
    public List<Order> searchOrdersByOrderId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return getAllOrders();
        }
        String trimmedId = orderId.trim();
        
        // 先嘗試精確匹配（如果是完整數字）
        try {
            Long exactId = Long.parseLong(trimmedId);
            Optional<Order> exactOrder = orderRepository.findByOrderId(exactId);
            if (exactOrder.isPresent()) {
                return List.of(exactOrder.get());
            }
        } catch (NumberFormatException e) {
            // 如果不是數字，使用模糊搜尋
        }
        
        // 模糊搜尋（部分匹配）
        return orderRepository.searchByOrderIdContaining(trimmedId);
    }
    
    /**
     * 建立新訂單
     * 會自動計算最終金額（包含折扣）
     * 
     * @param order 要建立的訂單物件
     * @return 建立成功後的訂單物件（包含自動產生的訂單ID和計算後的最終金額）
     * @throws RuntimeException 如果訂單資料驗證失敗
     */
    public Order createOrder(Order order) {
        // 計算折扣後的價格（故意留一個小 bug：沒有檢查 discount 是否超過 100%）
        calculateFinalAmount(order);
        return orderRepository.save(order);
    }
    
    /**
     * 更新訂單資訊
     * 會更新訂單的所有欄位並重新計算最終金額
     * 
     * @param orderId 要更新的訂單ID
     * @param orderDetails 包含更新資料的訂單物件
     * @return 更新後的訂單物件
     * @throws RuntimeException 如果訂單不存在
     */
    public Order updateOrder(Long orderId, Order orderDetails) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setUsername(orderDetails.getUsername());
            order.setAmount(orderDetails.getAmount());
            order.setCurrency(orderDetails.getCurrency());
            order.setStatus(orderDetails.getStatus());
            order.setDiscount(orderDetails.getDiscount());
            calculateFinalAmount(order);
            return orderRepository.save(order);
        }
        throw new RuntimeException("訂單不存在: " + orderId);
    }
    
    /**
     * 刪除訂單
     * 
     * @param orderId 要刪除的訂單ID
     */
    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }
    
    /**
     * 計算訂單的最終金額（包含折扣）
     * 計算公式：最終金額 = 原始金額 - (原始金額 × 折扣百分比 / 100)
     * 
     * @param order 要計算最終金額的訂單物件
     * @apiNote 故意留一個不優化的地方：沒有使用 BigDecimal 的比較方法來檢查折扣是否超過 100%
     */
    private void calculateFinalAmount(Order order) {
        BigDecimal amount = order.getAmount();
        BigDecimal discount = order.getDiscount() != null ? order.getDiscount() : BigDecimal.ZERO;
        
        // 計算折扣金額
        BigDecimal discountAmount = amount.multiply(discount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        
        // 計算最終金額
        BigDecimal finalAmount = amount.subtract(discountAmount);
        order.setFinalAmount(finalAmount);
    }
    
    /**
     * 將訂單金額轉換為新台幣（TWD）
     * 根據訂單的原始幣別和最終金額進行轉換
     * 
     * @param orderId 訂單ID
     * @return 轉換後的新台幣金額（保留2位小數）
     * @throws RuntimeException 如果訂單不存在
     */
    public BigDecimal convertToTwd(Long orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            return currencyService.convertToTwd(order.getFinalAmount(), order.getCurrency());
        }
        throw new RuntimeException("訂單不存在: " + orderId);
    }
    
    /**
     * 將訂單金額轉換為指定幣別
     * 轉換流程：訂單原始幣別 → TWD → 目標幣別
     * 
     * @param orderId 訂單ID
     * @param targetCurrency 目標幣別代碼（如：USD, EUR, JPY）
     * @return 轉換後的目標幣別金額（保留2位小數）
     * @throws RuntimeException 如果訂單不存在或目標幣別不存在
     */
    public BigDecimal convertCurrency(Long orderId, String targetCurrency) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            BigDecimal amount = order.getFinalAmount();
            String sourceCurrency = order.getCurrency();
            return currencyService.convertCurrency(amount, sourceCurrency, targetCurrency);
        }
        throw new RuntimeException("訂單不存在: " + orderId);
    }
}

