package com.example.ordersystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OrderCurrencyApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderCurrencyApplication.class, args);
    }
}

