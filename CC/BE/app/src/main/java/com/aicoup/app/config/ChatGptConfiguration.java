package com.aicoup.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * ChatGPT단 연결 소켓 관련 Config
 */
@Configuration
public class ChatGptConfiguration {

    @Value("${gpt.openai.secret-key}")
    private String secretKey;

    @Bean
    public RestTemplate template(){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Authorization", "Bearer " + secretKey);
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}
