package com.aicoup.app.pipeline.aiot.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MMResponse {
    Integer left_card;
    Integer right_card;
    List<Integer> extra_card = new ArrayList<>();
}
