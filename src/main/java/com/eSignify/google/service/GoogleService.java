package com.eSignify.google.service;

import java.util.Base64;
import java.util.HashMap;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.eSignify.common.CommonUtil;
import com.google.gson.JsonObject;

@Service
public class GoogleService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Autowired
	CommonUtil commonUtil;
    
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
     * @param String accessToken,String pdfUrl, HashMap<String, Object> custMap
    		,HashMap<String, Object> pdfMap ,HashMap<String, Object> mailMap
     * @return ResponseEntity
     */
    public String sendEmail(String accessToken,String filePath, HashMap<String, Object> custMap
    		,HashMap<String, Object> pdfMap ,HashMap<String, Object> mailMap) {
    	
    	String url = "https://www.googleapis.com/gmail/v1/users/me/messages/send";
    	
    	String custCd 	  = (String) custMap.get("cust_cd");
    	String custNm 	  = (String) custMap.get("cust_nm");
    	String custGooId  = "nxnx23@naver.com";//(String) custMap.get("cust_gooid");
    	String cust_kaid  = "nxnx23@naver.com";//(String) custMap.get("cust_kaid");
    	String subject 	  = (String) mailMap.get("form_title");
    	String bodyText   = (String) mailMap.get("form_detail");
    	
    	subject =  "=?utf-8?B?" + commonUtil.comEncode(subject)+ "?=";
    	String rawMessage = createRawEmail(cust_kaid,subject, bodyText);
    	
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
    	
		
		String tableName = "t_mail_hist";
        String user_id = "auddnjssk";
        
        // JSON 데이터 동적으로 생성
        JsonObject supaBaseBody = new JsonObject();
        supaBaseBody.addProperty("user_id",user_id);
        supaBaseBody.addProperty("cust_cd",custCd);
        supaBaseBody.addProperty("subject",(String) mailMap.get("form_title"));
        supaBaseBody.addProperty("content",(String) mailMap.get("form_detail"));
        
        
        System.out.println(supaBaseBody.toString());
        
        
		commonUtil.supaBaseInsert(tableName, supaBaseBody);
		
    	
    	return response.getBody();
    }

    private String createRawEmail(String to, String subject, String bodyText) {
        try {
        	
            String raw = "To: " + to + "\r\n" +
                         "Subject: " + subject + "\r\n" +
                         "Content-Type: text/html; charset=utf-8\r\n\r\n" +
                         bodyText;

            // Base64 URL-safe 인코딩
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException("Error creating raw email", e);
        }
    }

}
