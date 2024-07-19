package com.aicoup.app.pipeline.aiot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AIoTSocketImpl implements AIoTSocket {

    @Value("${aiot.url}")
    String url;

    @Override
    public String getDataFromAIoTServer() {
        WebClient webClient = WebClient.builder().build();
        Mono<String> resp = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class);
        return resp.block();
    }
}
