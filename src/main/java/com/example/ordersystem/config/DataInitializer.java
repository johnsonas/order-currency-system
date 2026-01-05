package com.example.ordersystem.config;

import com.example.ordersystem.model.Menu;
import com.example.ordersystem.model.Role;
import com.example.ordersystem.model.User;
import com.example.ordersystem.repository.MenuRepository;
import com.example.ordersystem.repository.RoleRepository;
import com.example.ordersystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MenuRepository menuRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // 初始化角色
        Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName("ADMIN");
                    return roleRepository.save(role);
                });
        
        roleRepository.findByRoleName("USER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName("USER");
                    return roleRepository.save(role);
                });
        
        // 創建或更新預設管理員帳號
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            // 如果不存在，創建新的管理員帳號
            admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@example.com");
            admin.setEnabled(true);
            admin.setRoles(new HashSet<>());
            System.out.println("預設管理員帳號已創建: admin / admin123");
        } else {
            // 如果已存在，確保有 ADMIN 角色
            System.out.println("檢查並更新管理員帳號角色...");
        }
        
        // 確保管理員帳號有 ADMIN 角色
        Set<Role> adminRoles = admin.getRoles();
        if (adminRoles == null) {
            adminRoles = new HashSet<>();
            admin.setRoles(adminRoles);
        }
        if (!adminRoles.contains(adminRole)) {
            adminRoles.add(adminRole);
            System.out.println("已為管理員帳號添加 ADMIN 角色");
        }
        
        userRepository.save(admin);
        
        // 初始化選單
        initializeMenus(adminRole);
    }
    
    private void initializeMenus(Role adminRole) {
        // 訂單列表選單（所有登入用戶都可以訪問）
        menuRepository.findByMenuKey("orders")
                .orElseGet(() -> {
                    Menu menu = new Menu();
                    menu.setMenuKey("orders");
                    menu.setLabel("訂單列表");
                    menu.setIcon("📋");
                    menu.setRoute("orders");
                    menu.setSortOrder(1);
                    menu.setEnabled(true);
                    menu.setRequiredRoles(new HashSet<>());
                    return menuRepository.save(menu);
                });
        
        // 幣別轉換系統選單（所有登入用戶都可以訪問）
        menuRepository.findByMenuKey("currency")
                .orElseGet(() -> {
                    Menu menu = new Menu();
                    menu.setMenuKey("currency");
                    menu.setLabel("幣別轉換系統");
                    menu.setIcon("💱");
                    menu.setRoute("currency");
                    menu.setSortOrder(2);
                    menu.setEnabled(true);
                    menu.setRequiredRoles(new HashSet<>());
                    return menuRepository.save(menu);
                });
        
        // 匯率管理選單（僅管理員）
        Menu ratesMenu = menuRepository.findByMenuKey("rates")
                .orElseGet(() -> {
                    Menu menu = new Menu();
                    menu.setMenuKey("rates");
                    menu.setLabel("匯率管理");
                    menu.setIcon("📊");
                    menu.setRoute("rates");
                    menu.setSortOrder(3);
                    menu.setEnabled(true);
                    menu.setRequiredRoles(new HashSet<>());
                    return menuRepository.save(menu);
                });
        
        // 確保匯率管理選單有關聯到 ADMIN 角色
        Set<Role> ratesMenuRoles = ratesMenu.getRequiredRoles();
        if (ratesMenuRoles == null) {
            ratesMenuRoles = new HashSet<>();
            ratesMenu.setRequiredRoles(ratesMenuRoles);
        }
        if (!ratesMenuRoles.contains(adminRole)) {
            ratesMenuRoles.add(adminRole);
            menuRepository.save(ratesMenu);
            System.out.println("已為匯率管理選單添加 ADMIN 角色要求");
        }
    }
}

