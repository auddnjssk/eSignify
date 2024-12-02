package com.eSignify.common.kakao.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.eSignify.common.kakao.AccessTokenResponse;

@Service
public class KakaoSendService {

    private final RestTemplate restTemplate = new RestTemplate();

    // 친구 목록 조회
    public String getFriends(AccessTokenResponse accessToken) {
    	String url = "https://kapi.kakao.com/v1/api/talk/friends";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken.getAccessToken());

        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }

    // 친구에게 메시지 보내기
    public String sendMessage(String accessToken, String friendId, String messageText) {
        String url = "https://kapi.kakao.com/v1/api/talk/friends/message/default/send";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        Map<String, Object> templateObject = new HashMap<>();
        templateObject.put("object_type", "text");
        templateObject.put("text", messageText);
        templateObject.put("link", Map.of("web_url", "http://yourwebsite.com", "mobile_web_url", "http://yourwebsite.com"));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("receiver_uuids", new String[]{friendId});
        requestBody.put("template_object", templateObject);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return response.getBody();
    }
}
