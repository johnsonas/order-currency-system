package com.example.ordersystem.controller;

import com.example.ordersystem.dto.JwtResponse;
import com.example.ordersystem.dto.LoginRequest;
import com.example.ordersystem.dto.MenuItem;
import com.example.ordersystem.dto.RegisterRequest;
import com.example.ordersystem.model.Menu;
import com.example.ordersystem.model.User;
import com.example.ordersystem.repository.MenuRepository;
import com.example.ordersystem.service.UserService;
import com.example.ordersystem.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private MenuRepository menuRepository;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "使用者名稱或密碼錯誤");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        
        UserDetails userDetails = userService.loadUserByUsername(loginRequest.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        
        return ResponseEntity.ok(new JwtResponse(token, loginRequest.getUsername()));
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User user = userService.registerUser(
                registerRequest.getUsername(),
                registerRequest.getPassword(),
                registerRequest.getEmail()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "註冊成功");
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    /**
     * 獲取當前用戶信息（用於調試）
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "未登入");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", authentication.getName());
        userInfo.put("authorities", authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
        userInfo.put("authenticated", authentication.isAuthenticated());
        
        return ResponseEntity.ok(userInfo);
    }
    
    /**
     * 獲取用戶可訪問的選單列表
     * 從資料庫讀取選單，根據用戶角色動態過濾
     */
    @GetMapping("/menu")
    public ResponseEntity<?> getMenu() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "未登入");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        
        // 獲取用戶角色
        List<String> userRoles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());
        
        // 從資料庫讀取所有啟用的選單（按排序順序）
        List<Menu> allMenus = menuRepository.findAllEnabledMenus();
        
        // 轉換為 MenuItem DTO 並根據用戶角色過濾
        List<MenuItem> accessibleMenus = allMenus.stream()
            .filter(menu -> {
                // 如果選單不需要特定角色（空集合），則所有登入用戶都可以訪問
                if (menu.getRequiredRoles() == null || menu.getRequiredRoles().isEmpty()) {
                    return true;
                }
                // 檢查用戶是否有選單要求的任一角色
                return menu.getRequiredRoles().stream()
                    .map(role -> "ROLE_" + role.getRoleName())
                    .anyMatch(userRoles::contains);
            })
            .map(menu -> {
                // 轉換 Menu 實體為 MenuItem DTO
                List<String> requiredRoleNames = menu.getRequiredRoles().stream()
                    .map(role -> "ROLE_" + role.getRoleName())
                    .collect(Collectors.toList());
                
                return new MenuItem(
                    menu.getMenuKey(),
                    menu.getLabel(),
                    menu.getIcon(),
                    menu.getRoute(),
                    requiredRoleNames
                );
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(accessibleMenus);
    }
}
