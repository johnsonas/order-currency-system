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
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @GetMapping
    public ResponseEntity<Page<Order>> getAllOrders(
            @RequestParam(required = false) String searchOrderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders;
        
        if (searchOrderId != null && !searchOrderId.trim().isEmpty()) {
            orders = orderService.searchOrdersByOrderId(searchOrderId, pageable);
        } else {
            orders = orderService.getAllOrders(pageable);
        }
        
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {

            Optional<Order> order = orderService.getOrderById(id);
            return order.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }
    

    @GetMapping("/username/{username}")
    public ResponseEntity<List<Order>> getOrdersByUsername(@PathVariable String username) {
        List<Order> orders = orderService.getOrdersByUsername(username);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }
    
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody Order order) {
        Order createdOrder = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @Valid @RequestBody Order order) {
        try {
            Order updatedOrder = orderService.updateOrder(id, order);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/convert/twd")
    public ResponseEntity<BigDecimal> convertToTwd(@PathVariable Long id) {
        try {
            BigDecimal twdAmount = orderService.convertToTwd(id);
            return ResponseEntity.ok(twdAmount);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{id}/convert/{targetCurrency}")
    public ResponseEntity<BigDecimal> convertCurrency(
            @PathVariable Long id,
            @PathVariable String targetCurrency) {
        try {
            CurrencyCode currencyCode = CurrencyCode.fromCode(targetCurrency);
            if (currencyCode == null) {
                return ResponseEntity.badRequest().build();
            }
            BigDecimal convertedAmount = orderService.convertCurrency(id, currencyCode);
            return ResponseEntity.ok(convertedAmount);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    
}

