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

import com.eSignify.model.AccessTokenResponse;

import okhttp3.*;
import okhttp3.MediaType;

@Service
public class KakaoSendService {

    private final RestTemplate restTemplate = new RestTemplate();

    // ģ������ �޽��� ������
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
    
    // ������ �޽��� ������
    public void sendMessage2Me(String accessToken) {
        OkHttpClient client = new OkHttpClient();

        String url = "https://kapi.kakao.com/v2/api/talk/memo/default/send";

        // �޽��� ���� ���� (JSON ����)
        String json = "{"
                + "\"template_object\": {"
                + "  \"object_type\": \"text\","
                + "  \"text\": \"�ȳ��ϼ���, īī�� �޽��� API �����Դϴ�.\","
                + "  \"link\": {"
                + "    \"web_url\": \"https://yourwebsite.com\","
                + "    \"mobile_web_url\": \"https://yourwebsite.com\""
                + "  }"
                + "}"
                + "}";

        // ��û ���� �ۼ� (application/json; charset=utf-8 -> application/json)
        //RequestBody jsonPars = RequestBody.create(json, MediaType.parse("application/json"));
        // JSON ���ڿ��� URL ���ڵ�
        String encodedJson;
        try {
            encodedJson = URLEncoder.encode(json, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // URL ���ڵ��� ��û ���� �ۼ�
        RequestBody body = new FormBody.Builder()
                .add("template_object", encodedJson)  // URL ���ڵ��� JSON ���ڿ� �߰�
                .build();
        // HTTP ��û ����
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + accessToken)  // �׼��� ��ū �߰�
                .addHeader("Content-Type", "application/x-www-form-urlencoded")  // Content-Type ��� ����
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("�޽��� ���� ����: " + response.body().string());
            } else {
                System.out.println("�޽��� ���� ����: " + response.code() + " - " + response.message());
                System.out.println("���� ���� ����: " + response.body().string());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
