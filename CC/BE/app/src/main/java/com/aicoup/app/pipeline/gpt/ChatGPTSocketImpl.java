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

    @Value("${gpt.openai.model1}")
    private String model1;
    @Value("${gpt.openai.model2}")
    private String model2;
    @Value("${gpt.openai.model3}")
    private String model3;
    @Value("${gpt.openai.model4}")
    private String model4;
    @Value("${gpt.openai.model5}")
    private String model5;

    @Value("${gpt.openai.api-url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    @Override
    public String getDataFromGptApiForAction(String systemPrompt, String userPrompt) {
        return prompt(model1, systemPrompt, userPrompt, "A");
    }

    @Override
    public String getDataFromGptApiForChallengeAgainstAction(String systemPrompt, String userPrompt) {
        return prompt(model2, systemPrompt, userPrompt, "B");
    }

    @Override
    public String getDataFromGptApiForCounteractionAgainstAction(String systemPrompt, String userPrompt) {
        return prompt(model3, systemPrompt, userPrompt, "C");
    }

    @Override
    public String getDataFromGptApiForChallengeAgainstCounteraction(String systemPrompt, String userPrompt) {
        return prompt(model4, systemPrompt, userPrompt, "D");
    }

    @Override
    public String getDataFromGptApiForDialog(String systemPrompt, String userPrompt) {
        return prompt(model5, systemPrompt, userPrompt, "E");
    }

    private String prompt(String model, String systemPrompt, String userPrompt, String state) {
        ChatGPTRequest request = new ChatGPTRequest(model, systemPrompt, userPrompt);
        ChatGPTResponse chatGPTResponse = restTemplate.postForObject(apiUrl, request, ChatGPTResponse.class);
        return chatGPTResponse.getChoices().get(0).getMessage().getContent();
    }
}
