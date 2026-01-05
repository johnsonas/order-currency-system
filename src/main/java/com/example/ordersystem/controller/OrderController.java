package com.example.ordersystem.controller;

import com.example.ordersystem.model.CurrencyCode;
import com.example.ordersystem.model.Order;
import com.example.ordersystem.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<Order>> getAllOrders(
            @RequestParam(required = false) String searchOrderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orders;
            
            if (searchOrderId != null && !searchOrderId.trim().isEmpty()) {
                if (isAdmin) {
                    orders = orderService.searchOrdersByOrderId(searchOrderId, pageable);
                } else {
                    orders = orderService.searchOrdersByOrderIdAndUsername(searchOrderId, currentUsername, pageable);
                }
            } else {
                if (isAdmin) {
                    orders = orderService.getAllOrders(pageable);
                } else {
                    orders = orderService.getOrdersByUsername(currentUsername, pageable);
                }
            }
            
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(OrderController.class)
                .error("獲取訂單列表失敗", e);
            throw e; // 讓 GlobalExceptionHandler 處理
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Optional<Order> order = orderService.getOrderById(id);
        return order.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    

    @GetMapping("/username/{username}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<Order>> getOrdersByUsername(@PathVariable String username) {
        List<Order> orders = orderService.getOrdersByUsername(username);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Order> createOrder(@Valid @RequestBody Order order) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        
        // 如果不是管理員，強制使用當前登入用戶名
        if (!isAdmin) {
            order.setUsername(currentUsername);
        }
        
        Order createdOrder = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @Valid @RequestBody Order order) {
        Order updatedOrder = orderService.updateOrder(id, order);
        return ResponseEntity.ok(updatedOrder);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/convert/twd")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<BigDecimal> convertToTwd(@PathVariable Long id) {
        BigDecimal twdAmount = orderService.convertToTwd(id);
        return ResponseEntity.ok(twdAmount);
    }
    
    @GetMapping("/{id}/convert/{targetCurrency}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<BigDecimal> convertCurrency(
            @PathVariable Long id,
            @PathVariable String targetCurrency) {
        CurrencyCode currencyCode = CurrencyCode.fromCode(targetCurrency);
        if (currencyCode == null) {
            throw new com.example.ordersystem.exception.BadRequestException("無效的幣別代碼: " + targetCurrency);
        }
        BigDecimal convertedAmount = orderService.convertCurrency(id, currencyCode);
        return ResponseEntity.ok(convertedAmount);
    }


    
}

