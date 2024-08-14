package com.aicoup.app.pipeline.gpt;

import java.util.List;
import java.util.Map;

public interface ChatGPTSocket {
    // Action
    String getDataFromGptApiForAction(String systemPrompt, String userPrompt);

    // Action에 대한 Challenge
    String getDataFromGptApiForChallengeAgainstAction(String systemPrompt, String userPrompt);

    // Action에 대한 Counteraction
    String getDataFromGptApiForCounteractionAgainstAction(String systemPrompt, String userPrompt);

    // Counteraction에 대한 Challenge
    String getDataFromGptApiForChallengeAgainstCounteraction(String systemPrompt, String userPrompt);

    String getDataFromGptApiForDialog(String systemPrompt, String userPrompt);
}
