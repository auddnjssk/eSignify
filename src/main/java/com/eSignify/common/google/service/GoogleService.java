package com.eSignify.common.google.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

import org.json.JSONObject;

@Service
public class GoogleService {
    
    private final RestTemplate restTemplate = new RestTemplate();

	@Value("${my.client_id}")
	private String clientId;
	
	@Value("${my.redirect_uri}")
	private String redirectUri;
	
	@Value("${my.client_secret}")
	private String clientSecret;
	
	String tokenUrl = "https://oauth2.googleapis.com/token";
	
    public ResponseEntity<String> getGoogleToken(String authorizationCode) {
    	
	    

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

	    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
	    params.add("code", authorizationCode);
	    params.add("client_id", clientId);
	    params.add("client_secret", clientSecret);
	    params.add("redirect_uri", redirectUri);
	    params.add("grant_type", "authorization_code");

	    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

	    try {
	        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);

	        if (response.getStatusCode().is2xxSuccessful()) {
	            JSONObject json = new JSONObject(response.getBody());
	            String accessToken = json.getString("access_token");
	            String refreshToken = json.getString("refresh_token");

                // Create HttpOnly cookies
                ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                        .httpOnly(true)
                        .secure(true) // Use HTTPS in production
                        .path("/")
                        .maxAge(3600) // Access Token validity
                        .build();
                
                ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(2592000) // 30 days validity for Refresh Token
                        .build();
                

	            // 성공 로그
	            System.out.println("Access Token: " + accessToken);
	            return ResponseEntity.ok("Login successful");
	        } else {
	            System.err.println("Error Response: " + response.getBody());
	            throw new RuntimeException("Failed to get access token: " + response.getBody());
	        }
	    } catch (HttpClientErrorException e) {
	        System.err.println("HTTP Status: " + e.getStatusCode());
	        System.err.println("Response Body: " + e.getResponseBodyAsString());
	        throw new RuntimeException("Failed to get access token: " + e.getResponseBodyAsString(), e);
	    }
	}
    
    public String refreshAccessToken(String refreshToken) {
        String url = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", "YOUR_CLIENT_ID");
        params.add("client_secret", "YOUR_CLIENT_SECRET");
        params.add("refresh_token", refreshToken);
        params.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        return response.getBody(); // 새로운 Access Token 반환
    }

    
    /**
     * @param String accessToken , to , subject , bodyText
     * @return ResponseEntity
     */
    public String sendEmail(Map<String, String> requestBody,String accessToken, String to, String subject, String bodyText) {
    	
        String url = "https://www.googleapis.com/gmail/v1/users/me/messages/send";
        
    	String CUST_CD = requestBody.get("CUST_CD");
    	String CUST_NM = requestBody.get("CUST_NM");
    	String CUST_GOOID = requestBody.get("CUST_GOOID");
    	String filePath = requestBody.get("filePath");
    	
        to = "nxnx23@naver.com";
        
        subject = "서브젝트";
        
        
        bodyText = "안녕하세요 해당 URL에 접속하셔서 사인 부탁드립니다." + filePath;
        
        String rawMessage = createRawEmail(to, subject, bodyText);
        
        //ResponseEntity<String> ResponseEntity =  getAccessToken(accessToken);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        String rawMessageJson = "{ \"raw\": \"" + rawMessage + "\" }";
        HttpEntity<String> entity = new HttpEntity<>(rawMessageJson, headers);
        ResponseEntity<String> response = null;

        try {
        	response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        }catch (HttpClientErrorException e) {
        	   System.err.println("HTTP Status: " + e.getStatusCode());
        	    System.err.println("Response Body: " + e.getResponseBodyAsString());
			System.out.println(e.getMessage());
		}
        
        return response.getBody();
    }

    private String createRawEmail(String to, String subject, String bodyText) {
        try {
            String raw = "To: " + to + "\r\n" +
                         "Subject: " + subject + "\r\n" +
                         "Content-Type: text/plain; charset=utf-8\r\n\r\n" +
                         bodyText;

            // Base64 URL-safe 인코딩
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException("Error creating raw email", e);
        }
    }

}
