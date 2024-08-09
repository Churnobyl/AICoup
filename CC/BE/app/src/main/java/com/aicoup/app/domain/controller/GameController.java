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

/**
 * Game 관련 REST api
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class GameController {

    private final GameMemberRepository gameMemberRepository;

    @Value("${cookie.name}")
    private String cookieName;

    /**
     * 쿠키 기반 기존 게임 정보가 있는지 확인하는 api
     * @param request
     * @return 매치하는 게임 정보가 있으면 true / 없으면 false
     */
    @GetMapping("/status-check")
    public ResponseEntity<?> statusCheck(HttpServletRequest request) {
        // 쿠키 가져오기
        Cookie[] cookies = request.getCookies();

        // 쿠키 자체가 없으면 false 리턴
        if (cookies == null) return ResponseEntity.ok(false);

        Cookie cookie = null;

        // coockieName에 해당하는 key 있으면 설정
        for (Cookie c : cookies) {
            if (cookieName.equals(c.getName())) {
                cookie = c;
                break;
            }
        }

        // 해당하는 쿠키 없으면 false
        if (cookie == null) return ResponseEntity.ok(false);

        // Redis에서 매당하는 value 체크
        boolean byNameExists = gameMemberRepository.existsGameMembersByName(cookie.getValue());

        log.info("[status-check] {}", byNameExists);
        return ResponseEntity.ok(byNameExists);
    }
}
