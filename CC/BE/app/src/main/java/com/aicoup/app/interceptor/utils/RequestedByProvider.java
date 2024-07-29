package com.aicoup.app.interceptor.utils;

import java.util.Optional;

public interface RequestedByProvider {
    Optional<String> getRequestedBy();
}
