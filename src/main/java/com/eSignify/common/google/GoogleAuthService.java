package com.eSignify.common.google;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

@Service
public class GoogleAuthService {
    
    private final RestTemplate restTemplate = new RestTemplate();

	@Value("${my.client_id}")
	private String clientId;
	
	@Value("${my.redirect_uri}")
	private String redirectUri;
	
	@Value("${my.client_secret}")
	private String clientSecret;
	
	
    public String getAccessToken(String authorizationCode) {
        String url = "https://oauth2.googleapis.com/token";

        // 요청 바디 설정 
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String requestBody = "code=" + authorizationCode +
                             "&client_id=" + clientId +
                             "&client_secret=" + clientSecret +
                             "&redirect_uri=" + redirectUri +
                             "&grant_type=authorization_code";

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        // 요청 전송
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            // 응답에서 액세스 토큰 추출
            JSONObject json = new JSONObject(response.getBody());
            return json.getString("access_token");
        } else {
            throw new RuntimeException("Failed to get access token: " + response.getStatusCode());
        }
    }
}
