package com.aicoup.app.interceptor.utils;

import com.aicoup.app.interceptor.authentication.Authentication;

import java.util.Optional;

public interface AuthenticationHolder {
    Optional<Authentication> getAuthentication();
    void setAuthentication(Authentication authentication);
}
