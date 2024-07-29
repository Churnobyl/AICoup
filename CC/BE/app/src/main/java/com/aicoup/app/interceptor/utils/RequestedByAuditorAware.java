package com.aicoup.app.interceptor.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RequestedByAuditorAware implements AuditorAware<String> {

    private static final String SYSTEM = "system";

    private final ApplicationContext applicationContext;

    public RequestedByAuditorAware(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            return Optional.of(applicationContext.getBean(RequestedByProvider.class))
                    .flatMap(RequestedByProvider::getRequestedBy);
        } catch (Exception e) {
            return Optional.of(SYSTEM); // 입력되지 않은 경우에는 기본값 "system" 으로 사용
        }
    }
}