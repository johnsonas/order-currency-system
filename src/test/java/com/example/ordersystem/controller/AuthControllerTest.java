package com.example.ordersystem.controller;

import com.example.ordersystem.dto.LoginRequest;
import com.example.ordersystem.dto.RegisterRequest;
import com.example.ordersystem.model.User;
import com.example.ordersystem.service.UserService;
import com.example.ordersystem.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 測試
 * 測試認證相關的 API 端點
 * 
 * @author Order Currency System
 * @version 1.0
 */
@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController 測試")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;
    
    @MockBean
    private com.example.ordersystem.filter.JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @MockBean
    private com.example.ordersystem.repository.MenuRepository menuRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDetails userDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        userDetails = org.springframework.security.core.userdetails.User.builder()
            .username("testuser")
            .password("encodedPassword")
            .authorities(Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER")
            ))
            .build();

        authentication = new UsernamePasswordAuthenticationToken(
            userDetails, 
            null, 
            userDetails.getAuthorities()
        );
    }

    @Test
    @DisplayName("測試登入 - 成功")
    void testLogin_Success() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("test-jwt-token");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("test-jwt-token"))
            .andExpect(jsonPath("$.username").value("testuser"));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, times(1)).loadUserByUsername("testuser");
        verify(jwtUtil, times(1)).generateToken(userDetails);
    }

    @Test
    @DisplayName("測試登入 - 密碼錯誤")
    void testLogin_WrongPassword() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("使用者名稱或密碼錯誤"));

        verify(userService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
    }

    @Test
    @DisplayName("測試註冊 - 成功")
    void testRegister_Success() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("newuser@example.com");

        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");

        when(userService.registerUser(eq("newuser"), eq("password123"), eq("newuser@example.com")))
            .thenReturn(newUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("註冊成功"))
            .andExpect(jsonPath("$.username").value("newuser"))
            .andExpect(jsonPath("$.email").value("newuser@example.com"));

        verify(userService, times(1)).registerUser(eq("newuser"), eq("password123"), eq("newuser@example.com"));
    }

    @Test
    @DisplayName("測試註冊 - 用戶名已存在")
    void testRegister_UsernameExists() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("existinguser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("existinguser@example.com");

        when(userService.registerUser(eq("existinguser"), eq("password123"), eq("existinguser@example.com")))
            .thenThrow(new RuntimeException("使用者名稱已存在"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("使用者名稱已存在"));

        verify(userService, times(1)).registerUser(eq("existinguser"), eq("password123"), eq("existinguser@example.com"));
    }

    @Test
    @DisplayName("測試獲取當前用戶信息 - 已登入")
    @org.springframework.security.test.context.support.WithMockUser(username = "testuser", roles = {"USER"})
    void testGetCurrentUser_Authenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.authenticated").value(true))
            .andExpect(jsonPath("$.authorities[0]").value("ROLE_USER"));
    }

    @Test
    @DisplayName("測試獲取當前用戶信息 - 未登入")
    void testGetCurrentUser_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("未登入"));
    }

    @Test
    @DisplayName("測試獲取選單 - ADMIN角色")
    @org.springframework.security.test.context.support.WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetMenu_Admin() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/menu"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[?(@.menuKey == 'orders')]").exists())
            .andExpect(jsonPath("$[?(@.menuKey == 'currency')]").exists())
            .andExpect(jsonPath("$[?(@.menuKey == 'rates')]").exists());
    }

    @Test
    @DisplayName("測試獲取選單 - USER角色")
    @org.springframework.security.test.context.support.WithMockUser(username = "testuser", roles = {"USER"})
    void testGetMenu_User() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/menu"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[?(@.menuKey == 'orders')]").exists())
            .andExpect(jsonPath("$[?(@.menuKey == 'currency')]").exists())
            .andExpect(jsonPath("$[?(@.menuKey == 'rates')]").doesNotExist()); // USER 不應該看到匯率管理
    }

    @Test
    @DisplayName("測試獲取選單 - 未登入")
    void testGetMenu_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/menu"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("未登入"));
    }
}

