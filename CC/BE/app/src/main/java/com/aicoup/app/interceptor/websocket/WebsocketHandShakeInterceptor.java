package com.aicoup.app.interceptor.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class WebsocketHandShakeInterceptor implements HandshakeInterceptor {


    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        HttpHeaders headers = request.getHeaders();
        List<String> cookies = headers.get(HttpHeaders.COOKIE);

        boolean cookieExists = false;
        if (cookies != null) {
            for (String cookie : cookies) {
                if (cookie.contains("aiCoup")) {
                    cookieExists = true;
                    break;
                }
            }
        }

        if (!cookieExists) {
            // 쿠키 만료 날짜 설정
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date expiryDate = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000); // 1일 후 만료
            String expires = sdf.format(expiryDate);

            // 쿠키 값 URL 인코딩
            String cookieValue = URLEncoder.encode("userA", "UTF-8");

            // 쿠키 설정
            response.getHeaders().add(HttpHeaders.SET_COOKIE,
                    "aiCoup=" + cookieValue + "; Path=/; HttpOnly; Secure; SameSite=None; Expires=" + expires + ";");
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
