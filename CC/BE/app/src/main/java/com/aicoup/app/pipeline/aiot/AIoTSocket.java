package com.aicoup.app.pipeline.aiot;

import com.aicoup.app.pipeline.aiot.dto.MMResponse;

import java.util.List;

public interface AIoTSocket {
    List<MMResponse> getDataFromAIoTServer();
}
