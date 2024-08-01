package com.aicoup.app.pipeline.gpt.converter;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter @Setter
public class GPTHistory {
    private Map<Integer, List<String>> history;

    public GPTHistory(Map<Integer, List<String>> history) {
        this.history = history;
    }
}
