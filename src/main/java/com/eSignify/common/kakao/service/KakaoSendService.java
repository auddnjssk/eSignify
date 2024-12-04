package com.eSignify.common.kakao.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.eSignify.common.kakao.AccessTokenResponse;

import okhttp3.*;
import okhttp3.MediaType;

@Service
public class KakaoSendService {

    private final RestTemplate restTemplate = new RestTemplate();

    // 친구에게 메시지 보내기
//    public String sendMessage(String accessToken, String friendId, String messageText) {
//        String url = "https://kapi.kakao.com/v1/api/talk/friends/message/default/send";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Authorization", "Bearer " + accessToken);
//
//        Map<String, Object> templateObject = new HashMap<>();
//        templateObject.put("object_type", "text");
//        templateObject.put("text", messageText);
//        templateObject.put("link", Map.of("web_url", "http://yourwebsite.com", "mobile_web_url", "http://yourwebsite.com"));
//
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("receiver_uuids", new String[]{friendId});
//        requestBody.put("template_object", templateObject);
//
//        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
//        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
//
//        return response.getBody();
//    }
    
    // 나에게 메시지 보내기
    public void sendMessage2Me(String accessToken) {
        OkHttpClient client = new OkHttpClient();

        String url = "https://kapi.kakao.com/v2/api/talk/memo/default/send";

        // 메시지 내용 설정 (JSON 형태)
        String json = "{"
                + "\"template_object\": {"
                + "  \"object_type\": \"text\","
                + "  \"text\": \"안녕하세요, 카카오 메시지 API 예제입니다.\","
                + "  \"link\": {"
                + "    \"web_url\": \"https://yourwebsite.com\","
                + "    \"mobile_web_url\": \"https://yourwebsite.com\""
                + "  }"
                + "}"
                + "}";

        // 요청 본문 작성 (application/json; charset=utf-8 -> application/json)
        //RequestBody jsonPars = RequestBody.create(json, MediaType.parse("application/json"));
        // JSON 문자열을 URL 인코딩
        String encodedJson;
        try {
            encodedJson = URLEncoder.encode(json, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // URL 인코딩된 요청 본문 작성
        RequestBody body = new FormBody.Builder()
                .add("template_object", encodedJson)  // URL 인코딩된 JSON 문자열 추가
                .build();
        // HTTP 요청 생성
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + accessToken)  // 액세스 토큰 추가
                .addHeader("Content-Type", "application/x-www-form-urlencoded")  // Content-Type 헤더 변경
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("메시지 전송 성공: " + response.body().string());
            } else {
                System.out.println("메시지 전송 실패: " + response.code() + " - " + response.message());
                System.out.println("에러 응답 본문: " + response.body().string());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
