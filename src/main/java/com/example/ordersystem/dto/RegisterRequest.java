package com.example.ordersystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    
    @NotBlank(message = "使用者名稱不能為空")
    @Size(min = 3, max = 50, message = "使用者名稱長度必須在3到50個字元之間")
    private String username;
    
    @NotBlank(message = "密碼不能為空")
    @Size(min = 6, message = "密碼長度至少6個字元")
    private String password;
    
    @Email(message = "電子郵件格式不正確")
    @NotBlank(message = "電子郵件不能為空")
    private String email;
    
    public RegisterRequest() {
    }
    
    public RegisterRequest(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}

