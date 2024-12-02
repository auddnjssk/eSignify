package com.eSignify.common.kakao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eSignify.common.kakao.service.KakaoAuthService;
import com.eSignify.common.kakao.service.KakaoSendService;

import okhttp3.*;
import org.springframework.http.HttpStatus;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@RestController
public class KaAuthController {

    @Autowired
    private KakaoAuthService kakaoAuthService;
    
    @Autowired
    private KakaoSendService kakaoSendService;

    
    // 확인하고 계속하기
    // https://kauth.kakao.com/oauth/authorize?client_id=afd0afadcf81f1efaf97fe02625c2963&redirect_uri=http://localhost:8080/oauth/kakao&response_type=code
    @GetMapping("/oauth/kakao")
    public AccessTokenResponse kakaoCallback(@RequestParam String code) throws Exception {
    	
        // authorization_code를 사용해 accessToken을 발급받습니다.
    	
    	AccessTokenResponse	tkResponse = kakaoAuthService.getAccessToken(code);
    	
        kakaoSendService.getFriends(tkResponse);
        
        
        return tkResponse;
    }
    
    
    @PostMapping("/send-message")
    public ResponseEntity<String> sendMessage(HttpServletRequest request) {
        // 쿠키에서 카카오 Access Token 가져오기
        String accessToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("kakaoAccessToken".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                }
            }
        }

        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
        }

        // 카카오 메시지 API 호출
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("template_object", "{\"object_type\":\"text\",\"text\":\"안녕하세요! 카카오 메시지입니다.\",\"link\":{\"web_url\":\"http://example.com\"}}")
                .build();

        Request requestToKakao = new Request.Builder()
                .url("https://kapi.kakao.com/v2/api/talk/memo/default/send")
                .post(formBody)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = client.newCall(requestToKakao).execute()) {
            if (response.isSuccessful()) {
                return ResponseEntity.ok("{\"success\":true}");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("메시지 전송 실패");
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
        }
    }
}
//http://localhost:8080/oauth/google
//https://accounts.google.com/o/oauth2/v2/auth?client_id=298618192214-r7d4c3p7jaavis7nghdrbug1bs0shkoh.apps.googleusercontent.com&redirect_uri=http://localhost:8080/oauth/google&response_type=code&scope=email%20profile&state=YOUR_UNIQUE_STATE
