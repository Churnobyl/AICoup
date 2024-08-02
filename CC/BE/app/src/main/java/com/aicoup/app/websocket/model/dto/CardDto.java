package com.aicoup.app.websocket.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardDto {
    private String name;
    private boolean revealed;
    private boolean influenceLost;
}