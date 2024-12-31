package com.eSignify.google.domain;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.eSignify.common.CommonUtil;
import com.eSignify.common.ObjectUtil;
import com.eSignify.google.entity.SendMailEntity;
import com.eSignify.google.service.GoogleService;
import com.eSignify.model.KakaoUserDTO;
import com.eSignify.model.LoginResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
public class GoogleController {
	
	@Autowired
	GoogleService googleService;

	@Autowired
	CommonUtil commonUtil;
	
	@Value("${my.client_id}")
	private String clientId;
	
	@Value("${my.redirect_uri}")
	private String redirectUri;
	
	@Value("${my.client_secret}")
	private String clientSecret;
	
	String tokenUrl = "https://oauth2.googleapis.com/token";
	private final RestTemplate restTemplate = new RestTemplate();
	
	
	
    @GetMapping("/oauth/google")
    @Operation(summary = "구글 oAuth2.0 리다이렉션", description = "Access Token, RefreshToken을 쿠키에 셋팅")
    public ResponseEntity<String> redirect(@RequestParam String code,HttpServletResponse response) throws Exception {
    	
    	HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

	    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
	    params.add("code", code);
	    params.add("client_id", clientId);
	    params.add("client_secret", clientSecret);
	    params.add("redirect_uri", redirectUri);
	    params.add("grant_type", "authorization_code");

	    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

	    try {
	        ResponseEntity<String> tokenResponse = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);

	        if (tokenResponse.getStatusCode().is2xxSuccessful()) {
	            JSONObject json = new JSONObject(tokenResponse.getBody());
	            String accessToken = json.getString("access_token");
	            String refreshToken = json.getString("refresh_token");

                // Create HttpOnly cookies
	            jakarta.servlet.http.Cookie googleAccess= new jakarta.servlet.http.Cookie("googleAccess", accessToken);
	            googleAccess.setHttpOnly(true);
	            googleAccess.setSecure(true); // Use HTTPS in production
	            googleAccess.setPath("/");
	            googleAccess.setMaxAge(3600); // Access Token validity
	            
	            // Create HttpOnly cookies
	            jakarta.servlet.http.Cookie googleRefresh= new jakarta.servlet.http.Cookie("googleRefresh", refreshToken);
	            googleRefresh.setHttpOnly(true);
	            googleRefresh.setSecure(true); // Use HTTPS in production
	            googleRefresh.setPath("/");
	            googleRefresh.setMaxAge(3600); // Access Token validity
                
	            response.addCookie(googleAccess);
	            response.addCookie(googleRefresh);

	            
	            // 성공 로그
	            System.out.println("Access Token: " + accessToken);
	            System.out.println("refreshToken Token: " + refreshToken);
	            
	            return ResponseEntity.ok("Login successful");
	        } else {
	            System.err.println("Error Response: " + tokenResponse.getBody());
	            throw new RuntimeException("Failed to get access token: " + tokenResponse.getBody());
	        }
	    } catch (HttpClientErrorException e) {
	        System.err.println("HTTP Status: " + e.getStatusCode());
	        System.err.println("Response Body: " + e.getResponseBodyAsString());
	        throw new RuntimeException("Failed to get access token: " + e.getResponseBodyAsString(), e);
	    }
    }
    
    
    @PostMapping("/google/connect")
    @Operation(summary = "구글계정 연동", description = "")
    public ResponseEntity<String> connect(
    		@CookieValue(name = "googleAccess", required = false) String googleAccessToken,
            @CookieValue(name = "googleRefresh", required = false) String googleRefreshToken,@RequestBody KakaoUserDTO kakaoUserDTO,HttpServletRequest request) throws Exception {
    	try {
    		
            String condition = "KAKAO_EMAIL=eq."+kakaoUserDTO.getEmail();
            
            String tableName = "T_USER";
            HttpSession session = request.getSession();
            String userId = (String) session.getAttribute("userId");
            
            List<LoginResponse> selectResponse= commonUtil.supaBaseSelect("", tableName,condition);
            
    		
            if (selectResponse != null) {
                return ResponseEntity.ok("īī�� ������ ���������� �����Ǿ����ϴ�.");
            } else {
            	condition = "USER_ID=eq."+userId;
            			
            	String body = "{\"KAKAO_ID\": \"" + kakaoUserDTO.getKakaoId() + "\", \"KAKAO_EMAIL\": \"" + kakaoUserDTO.getEmail() + "\"}";
            	
            	ResponseEntity<String> response =commonUtil.supaBaseUpdate(tableName,condition,body);
            	
            	System.out.println(response.getStatusCode());
            	
                return ResponseEntity.ok("���ο� ������ �����ǰ� īī�� ������ �����Ǿ����ϴ�.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("���� ���� �� ������ �߻��߽��ϴ�.");
        }
    }
    
    @PostMapping("/google/sendMail")
    @Operation(summary = "구글 API로 메일을 보냄.", description = "사용자,고객,계약서정보로 PDF계약서 생성후 구글드라이브에 저장, 이후에 SEND HIST테이블에 저장")
    public void sendMail(@CookieValue(name = "googleAccess", required = false) String googleAccessToken,
            @CookieValue(name = "googleRefresh", required = false) String googleRefreshToken,
            @RequestBody SendMailEntity requestBody,HttpServletRequest request) throws Exception {
    	
    	HashMap<String, Object> custMap = null ;
    	HashMap<String, Object> pdfMap = null ;
    	HashMap<String, Object> mailMap = null ;
    	 
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // JSON 문자열을 HashMap으로 변환
            custMap = objectMapper.readValue(requestBody.getCustMap(), HashMap.class);
            pdfMap = objectMapper.readValue(requestBody.getPdfMap(), HashMap.class);
            mailMap = objectMapper.readValue(requestBody.getMailMap(), HashMap.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    	if(googleAccessToken != null) {
	    	//PDF 생성
    		
    		commonUtil.createPDF(custMap,pdfMap,mailMap,googleAccessToken);



	        // 로그인된 구글 ID로 고객에게 메일을 뿌림
	        googleService.sendEmail(googleAccessToken,"pdfUrl",custMap,pdfMap,mailMap);
    	}
    	
    }
}
