package com.aicoup.app.pipeline.gpt.service;

public interface GameDataService {

    public String getGameDataAsJson(String gameId);
    public String getFormattedGameDataAsJson(String gameId);
}
