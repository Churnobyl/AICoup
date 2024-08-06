package com.aicoup.app.websocket.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardDto {
    private String name;
    private boolean revealed;
    private boolean influenceLost;
}