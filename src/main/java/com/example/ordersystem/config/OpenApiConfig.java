package com.example.ordersystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Currency System API")
                        .version("1.0.0")
                        .description("訂單與幣別轉換系統 REST API 文檔\n\n" +
                                "這是一個訂單管理與幣別轉換系統的 API，提供訂單 CRUD 操作和幣別轉換功能。")
                        .contact(new Contact()
                                .name("Order Currency System")
                                .email("support@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("本地開發環境")));
    }
}

