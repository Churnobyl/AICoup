package com.aicoup.app.websocket.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MessageDto {

    private String roomId;
    private String writer;
    private String state;
    private Object mainMessage;

    public MessageDto(String roomId, String writer) {
        this.roomId = roomId;
        this.writer = writer;
    }
}
