package com.aicoup.app.pipeline.aiot;

import com.aicoup.app.pipeline.aiot.dto.MMResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@PropertySource("classpath:application.yml")
public class AIoTSocketImpl implements AIoTSocket {

    @Value("${aiot.url}")
    String url;

    @Value("${aiot.port}")
    String port;

    @Value("${aiot.testApi}")
    String endpoint;

    String mockupData = "[\n" +
            "    {\n" +
            "        \"left_card\": 4,\n" +
            "        \"right_card\": 3,\n" +
            "        \"extra_card\": []\n" +
            "    },\n" +
            "    {\n" +
            "        \"left_card\": 1,\n" +
            "        \"right_card\": 2,\n" +
            "        \"extra_card\": []\n" +
            "    },\n" +
            "    {\n" +
            "        \"left_card\": 5,\n" +
            "        \"right_card\": 4,\n" +
            "        \"extra_card\": []\n" +
            "    },\n" +
            "    {\n" +
            "        \"left_card\": 1,\n" +
            "        \"right_card\": 2,\n" +
            "        \"extra_card\": []\n" +
            "    }\n" +
            "]";

    @Override
    public List<MMResponse> getDataFromAIoTServer() {
        WebClient webClient = WebClient.builder().build();
        Mono<String> resp = webClient
                .get()
                .uri(url + ":" + port + endpoint)
                .retrieve()
                .bodyToMono(String.class);

//        String resultString = resp.block();
        String resultString = mockupData;

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(
                    resultString,
                    new TypeReference<>() {
                    }
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
