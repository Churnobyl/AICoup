package com.aicoup.app.domain.controller;

import com.aicoup.app.domain.redisRepository.GameMemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class GameController {

    private final GameMemberRepository gameMemberRepository;

    @Value("${cookie.name}")
    private String cookieName;

    @GetMapping("/status-check")
    public ResponseEntity<?> statusCheck(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        System.out.println("cookies = " + Arrays.toString(cookies));

        if (cookies == null) return ResponseEntity.ok(false);

        Cookie cookie = null;

        for (Cookie c : cookies) {
            if (cookieName.equals(c.getName())) {
                cookie = c;
                break;
            }
        }

        if (cookie == null) return ResponseEntity.ok(false);
        System.out.println("cookie.getValue() = " + cookie.getValue());
        boolean byNameExists = gameMemberRepository.existsGameMembersByName(cookie.getValue());

        log.info("[status-check] {}", byNameExists);
        return ResponseEntity.ok(byNameExists);
    }
}
