package org.example.notificationsbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bot")
public class TelegramProperties {
    private String uel;
    private String name;
    private String token;
}
