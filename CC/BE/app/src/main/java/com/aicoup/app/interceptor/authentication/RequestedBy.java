package com.aicoup.app.interceptor.authentication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RequestedBy implements Authentication {
    private final String requestedBy;
}
