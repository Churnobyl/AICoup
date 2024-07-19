package com.aicoup.app.pipeline.gpt;

import java.util.List;
import java.util.Map;

public interface ChatGPTSocket {
    // Action
    String getDataFromGptApiForAction(String prompt);

    // Action에 대한 Challenge
    String getDataFromGptApiForChallengeAgainstAction(String prompt);

    // Action에 대한 Counteraction
    String getDataFromGptApiForCounteractionAgainstAction(String prompt);

    // Counteraction에 대한 Challenge
    String getDataFromGptApiForChallengeAgainstCounteraction(String prompt);
}
