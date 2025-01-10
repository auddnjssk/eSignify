package com.eSignify.google.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.eSignify.common.CommonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.http.HttpSession;

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
	
    public String  refreshAccessToken(String refreshToken) {
        RestTemplate restTemplate = new RestTemplate();

        // 요청 파라미터 설정
        Map<String, String> params = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "refresh_token", refreshToken,
                "grant_type", "refresh_token"
        );

        // POST 요청
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, params, Map.class);

        // 응답 처리
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> body = response.getBody();
            return (String) body.get("access_token");
        }

        throw new RuntimeException("Failed to refresh Google Access Token");
	};
    
    /**
     * @param String accessToken,String pdfUrl, HashMap<String, Object> custMap
    		,HashMap<String, Object> pdfMap ,HashMap<String, Object> mailMap
     * @return ResponseEntity
     */
    public String sendEmail(String accessToken,String filePath, HashMap<String, Object> custMap
    		,HashMap<String, Object> pdfMap ,HashMap<String, Object> mailMap, String fileId) {
    	
    	String url = "https://www.googleapis.com/gmail/v1/users/me/messages/send";
    	
    	String custCd 	  = (String) custMap.get("cust_cd");
    	String custNm 	  = (String) custMap.get("cust_nm");
    	String custGooId  = "nxnx23@naver.com";//(String) custMap.get("cust_gooid");
    	String cust_kaid  = "nxnx23@naver.com";//(String) custMap.get("cust_kaid");
    	String subject 	  = (String) mailMap.get("form_title");
    	String bodyText   = (String) mailMap.get("form_detail");
    	
    	
    	
    	subject =  "=?utf-8?B?" + commonUtil.comEncode(subject)+ "?=";
    	String rawMessage = createRawEmail(cust_kaid,subject, bodyText,fileId);
    	
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

    public String createRawEmail(String to, String subject, String bodyText,String fileId) {
        try {
        	
            String raw = "To: " + to + "\r\n" +
                         "Subject: " + subject + "\r\n" +
                         "Content-Type: text/html; charset=utf-8\r\n\r\n" +
                         bodyText +
                         "<br>"+ "<a href='http://localhost:8080/pdfAsImage?pdfPath=" + fileId + "'>계약서 서명하기</a>";
                        

            // Base64 URL-safe 인코딩
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException("Error creating raw email", e);
        }
    }
    
    
    public Map<String, Object> setUserInfoSession(String accessToken,HttpSession session) {
    	
    	String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
    	JsonObject jsonResponse = null ;
    	Map<String, Object> jsonMap = null;
    	
    	try {
            // 사용자 정보 요청 URL 생성
            URL url = new URL(userInfoUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            // 응답 읽기
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // 응답을 JSON으로 변환 (Gson 사용)
            String responseBody = response.toString();
            jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();


            session.setAttribute("GoogleUserId", jsonResponse.get("sub").getAsString());
            session.setAttribute("userName", jsonResponse.get("name").getAsString());
            session.setAttribute("userPicture", jsonResponse.get("picture").getAsString());
            session.setAttribute("email", jsonResponse.get("email").getAsString());
            
            ObjectMapper objectMapper = new ObjectMapper();
            
            jsonMap = objectMapper.readValue(jsonResponse.toString(), Map.class);

            
            
            return jsonMap;
        } catch (Exception e) {
            e.printStackTrace();
            return jsonMap;
        }
		
    }

}
