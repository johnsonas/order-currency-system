package com.example.ordersystem.repository;

import com.example.ordersystem.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByMenuKey(String menuKey);
    List<Menu> findByEnabledTrueOrderBySortOrderAsc();
    
    @Query("SELECT m FROM Menu m WHERE m.enabled = true ORDER BY m.sortOrder ASC")
    List<Menu> findAllEnabledMenus();
}

