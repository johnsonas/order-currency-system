package com.example.ordersystem.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StartupInfoListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(StartupInfoListener.class);

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        
        String baseUrl = "http://localhost:" + port + contextPath;
        
        logger.info("");
        logger.info("========================================");
        logger.info("應用程式啟動完成！");
        logger.info("========================================");
        logger.info("API 文檔 (Swagger UI): {}/swagger-ui/index.html", baseUrl);
        logger.info("OpenAPI JSON: {}/v3/api-docs", baseUrl);
        logger.info("OpenAPI YAML: {}/v3/api-docs.yaml", baseUrl);
        logger.info("========================================");
        logger.info("");
    }
}

