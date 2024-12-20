package com.eSignify.common.kakao.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.eSignify.model.AccessTokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class KakaoAuthService {

	// YOUR_CLIENT_ID = afd0afadcf81f1efaf97fe02625c2963
    // YOUR_REDIRECT_URI = http://localhost:8080/oauth/kakao
    //https://kauth.kakao.com/oauth/authorize?client_id=afd0afadcf81f1efaf97fe02625c2963&redirect_uri=http://localhost:8080/oauth/kakao&response_type=code
    private final ObjectMapper objectMapper = new ObjectMapper();
	
    private String clientId = "afd0afadcf81f1efaf97fe02625c2963";

    private String redirectUri = "http://localhost:8080/oauth/kakao";

    private final RestTemplate restTemplate = new RestTemplate();

    public AccessTokenResponse getAccessToken(String authorizationCode) throws Exception {
        String url = "https://kauth.kakao.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId); // īī�� �𺧷��۽����� �߱޹��� Client ID
        params.add("redirect_uri", redirectUri); // �����̷�Ʈ URI
        params.add("code", authorizationCode); // authorization code

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        // JSON ������ KakaoAccessTokenResponse ��ü�� ��ȯ
        return objectMapper.readValue(response.getBody(), AccessTokenResponse.class);
    }
    
    public AccessTokenResponse getRefreshAccessToken(String refreshToken) throws Exception {
        String url = "https://kauth.kakao.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", clientId); // īī�� �𺧷��۽����� �߱޹��� Client ID
        params.add("refresh_token", refreshToken); // ������ �߱޹��� Refresh Token

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        return objectMapper.readValue(response.getBody(), AccessTokenResponse.class);
    }
}
