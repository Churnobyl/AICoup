package com.aicoup.app.websocket.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
public class CardDto {
    private String name;
    private boolean revealed;
}