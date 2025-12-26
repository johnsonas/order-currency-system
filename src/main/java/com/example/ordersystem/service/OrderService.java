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

@Service
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CurrencyService currencyService;
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
    
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }
    
    public List<Order> getOrdersByUsername(String username) {
        return orderRepository.findByUsername(username);
    }
    
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }
    
    public Order createOrder(Order order) {
        // 計算折扣後的價格（故意留一個小 bug：沒有檢查 discount 是否超過 100%）
        calculateFinalAmount(order);
        return orderRepository.save(order);
    }
    
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
    
    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }
    
    // 計算最終金額（包含折扣）
    // 故意留一個不優化的地方：沒有使用 BigDecimal 的比較方法
    private void calculateFinalAmount(Order order) {
        BigDecimal amount = order.getAmount();
        BigDecimal discount = order.getDiscount() != null ? order.getDiscount() : BigDecimal.ZERO;
        
        // 計算折扣金額
        BigDecimal discountAmount = amount.multiply(discount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        
        // 計算最終金額
        BigDecimal finalAmount = amount.subtract(discountAmount);
        order.setFinalAmount(finalAmount);
    }
    
    // 幣別轉換（轉換為 TWD）
    public BigDecimal convertToTwd(Long orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            return currencyService.convertToTwd(order.getFinalAmount(), order.getCurrency());
        }
        throw new RuntimeException("訂單不存在: " + orderId);
    }
    
    // 幣別轉換（從某幣別轉換為目標幣別）
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

