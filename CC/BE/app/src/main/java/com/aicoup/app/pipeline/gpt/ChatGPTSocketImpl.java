package com.aicoup.app.pipeline.gpt;

import com.aicoup.app.pipeline.gpt.dto.ChatGPTRequest;
import com.aicoup.app.pipeline.gpt.dto.ChatGPTResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Component
@Service
@RequiredArgsConstructor
public class ChatGPTSocketImpl implements ChatGPTSocket {

    @Value("${gpt.openai.model}")
    private String model;

    @Value("${gpt.openai.api-url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    @Override
    public String getDataFromGptApiForAction(String prompt) {
        return prompt(prompt, "A");
    }

    @Override
    public String getDataFromGptApiForChallengeAgainstAction(String prompt) {
        return prompt(prompt, "B");
    }

    @Override
    public String getDataFromGptApiForCounteractionAgainstAction(String prompt) {
        return prompt(prompt, "C");
    }

    @Override
    public String getDataFromGptApiForChallengeAgainstCounteraction(String prompt) {
        return prompt(prompt, "D");
    }

    private String prompt(String prompt, String state) {
        ChatGPTRequest request = new ChatGPTRequest(model, prompt);
        ChatGPTResponse chatGPTResponse = restTemplate.postForObject(apiUrl, request, ChatGPTResponse.class);
        return chatGPTResponse.getChoices().get(0).getMessage().getContent();
    }
}
