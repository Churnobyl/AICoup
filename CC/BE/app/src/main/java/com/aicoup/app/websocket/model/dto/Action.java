package com.aicoup.app.websocket.model.dto;

import lombok.*;

@Getter
@AllArgsConstructor
public class Action {
    private String name;
    private int coinsNeeded;
    private boolean hasTarget;
}
