package com.aicoup.app.pipeline.gpt.service;

public interface GameDataService {

    String getGameDataAsJson(String gameId);
    String getFormattedGameDataAsJson(String gameId);
    String[] getData(String gameId);
}
