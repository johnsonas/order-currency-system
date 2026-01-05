package com.example.ordersystem.integration;

import com.example.ordersystem.dto.LoginRequest;
import com.example.ordersystem.dto.RegisterRequest;
import com.example.ordersystem.model.User;
import com.example.ordersystem.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 認證整合測試
 * 測試完整的認證流程，包括登入、註冊、獲取用戶信息等
 * 
 * @author Order Currency System
 * @version 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("認證整合測試")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String testToken;

    @BeforeEach
    void setUp() throws Exception {
        // 清理測試數據
        userRepository.deleteAll();

        // 創建測試用戶
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setEmail("test@example.com");
        testUser.setEnabled(true);
        userRepository.save(testUser);

        // 登入獲取 token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        testToken = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    @DisplayName("測試登入 - 成功")
    void testLogin_Success() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("測試登入 - 密碼錯誤")
    void testLogin_WrongPassword() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("使用者名稱或密碼錯誤"));
    }

    @Test
    @DisplayName("測試註冊 - 成功")
    void testRegister_Success() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("newuser@example.com");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("註冊成功"))
            .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @DisplayName("測試獲取當前用戶信息 - 已登入")
    void testGetCurrentUser_Authenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + testToken))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.authenticated").value(true));
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
    @DisplayName("測試獲取選單 - 已登入")
    void testGetMenu_Authenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/menu")
                .header("Authorization", "Bearer " + testToken))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[?(@.menuKey == 'orders')]").exists())
            .andExpect(jsonPath("$[?(@.menuKey == 'currency')]").exists());
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

