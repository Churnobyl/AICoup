package com.aicoup.app.pipeline.gpt.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatGPTRequest {
    private String model;
    private List<Message> messages;

    public ChatGPTRequest(String model, String systemPrompt, String userPrompt) {
        this.model = model;
        this.messages =  new ArrayList<>();
        this.messages.add(new Message("system", systemPrompt));
        this.messages.add(new Message("user", userPrompt));
    }
}