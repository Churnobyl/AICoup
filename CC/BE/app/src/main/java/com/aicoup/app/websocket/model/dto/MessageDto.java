package com.aicoup.app.websocket.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MessageDto {

    private String roomId;
    private String writer;
    private String state;
    private Object message;
}
