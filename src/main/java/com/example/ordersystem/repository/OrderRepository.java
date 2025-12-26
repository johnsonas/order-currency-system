package com.example.ordersystem.repository;

import com.example.ordersystem.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUsername(String username);
    
    List<Order> findByStatus(String status);
    
    @Query("SELECT o FROM Order o WHERE o.currency = :currency")
    List<Order> findByCurrency(String currency);
    
    Optional<Order> findByOrderId(Long orderId);
    
    @Query(value = "SELECT * FROM ORDERS WHERE TO_CHAR(ORDER_ID) LIKE '%' || :orderId || '%' ORDER BY CREATED_AT DESC", nativeQuery = true)
    List<Order> searchByOrderIdContaining(@Param("orderId") String orderId);
}


