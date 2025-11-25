package com.slowcoder.annotation.config;

import com.obs.services.ObsClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "huaweicloud.obs")
public class ObsConfig {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String baseUrl;

    @Bean
    public ObsClient obsClient() {
        return new ObsClient(accessKey, secretKey, endpoint);
    }
}
