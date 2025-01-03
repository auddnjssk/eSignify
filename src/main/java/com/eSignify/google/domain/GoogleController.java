package com.eSignify.google.domain;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import com.eSignify.common.PdfService;
import com.eSignify.google.entity.SendMailEntity;
import com.eSignify.google.service.GoogleService;
import com.eSignify.model.KakaoUserDTO;
import com.eSignify.model.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.drive.Drive;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
public class GoogleController {

	@Autowired
	GoogleService googleService;

	@Autowired
	CommonUtil commonUtil;

	@Autowired
	PdfService pdfService;
	
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
    public ResponseEntity<String> redirect(@RequestParam String code,HttpServletResponse response,HttpServletRequest request) throws Exception {
    	
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

                // Create cookies
	            jakarta.servlet.http.Cookie googleAccess= new jakarta.servlet.http.Cookie("googleAccess", accessToken);
	            googleAccess.setHttpOnly(true);
	            googleAccess.setSecure(true); 
	            googleAccess.setPath("/");
	            googleAccess.setMaxAge(3600 * 8); // 토큰 유호시간 8시간
	            
	            jakarta.servlet.http.Cookie googleRefresh= new jakarta.servlet.http.Cookie("googleRefresh", refreshToken);
	            googleRefresh.setHttpOnly(true);
	            googleRefresh.setSecure(true); 
	            googleRefresh.setPath("/");
	            googleRefresh.setMaxAge(3600 * 8); // 토큰 유호시간 8시간
                
	            response.addCookie(googleAccess);
	            response.addCookie(googleRefresh);
	            
	            // 성공 로그
	            System.out.println("Access Token: " + accessToken);
	            System.out.println("refreshToken Token: " + refreshToken);
	            
	            		
	            // 현재 요청의 프로토콜, 호스트, 포트 가져오기
	            String scheme = request.getScheme(); // http or https
	            String serverName = request.getServerName(); // localhost or domain
	            int serverPort = request.getServerPort(); // 8080 or port number

	            // 절대 경로 생성
	            String redirectUrl = scheme + "://" + serverName + ":" + serverPort + "/login-success";

	            HttpHeaders hd = new HttpHeaders();
	            headers.add("Location", redirectUrl);
	            
	            
	            return new ResponseEntity<>(hd, HttpStatus.FOUND); // HTTP 302 리다이렉션

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
    
    @PostMapping("/google/refresh")
    @Operation(summary = "구글 RefreshToken을 이용해 AccessToken 재발급", description = "")
    public ResponseEntity<?> refreshAccessToken(@CookieValue(name = "googleRefresh") String refreshToken
    		,@CookieValue(name = "googleAccess", required = false) String googleAccessToken,HttpServletResponse response) {
        try {
        	
        	if(googleAccessToken!="" || googleAccessToken != null) {
        		
        		return ResponseEntity.ok("Already AccessToken Ok");
        	};
        		
            // Refresh Token 검증
        	String newAccessToken = googleService.refreshAccessToken(refreshToken);
        	
            // Create cookies
            jakarta.servlet.http.Cookie googleAccess= new jakarta.servlet.http.Cookie("googleAccess", newAccessToken);
            googleAccess.setHttpOnly(true);
            googleAccess.setSecure(true); 
            googleAccess.setPath("/");
            googleAccess.setMaxAge(3600 * 8); // 토큰 유호시간 8시간
            
            response.addCookie(googleAccess);
            
            return ResponseEntity.ok("AccessToken Reissue successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid Refresh Token");
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
                return ResponseEntity.ok("");
            } else {
            	condition = "USER_ID=eq."+userId;
            			
            	String body = "{\"KAKAO_ID\": \"" + kakaoUserDTO.getKakaoId() + "\", \"KAKAO_EMAIL\": \"" + kakaoUserDTO.getEmail() + "\"}";
            	
            	ResponseEntity<String> response =commonUtil.supaBaseUpdate(tableName,condition,body);
            	
            	System.out.println(response.getStatusCode());
            	
                return ResponseEntity.ok("구글 연동 성공 !.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("���� ���� �� ������ �߻��߽��ϴ�.");
        }
    }
    
    @PostMapping("/google/sendMail")
    @Operation(summary = "구글 API로 메일을 보냄.", description = "사용자,고객,계약서정보로 PDF계약서 생성후 구글드라이브에 저장, 이후에 SEND HIST테이블에 저장")
    public ResponseEntity<String> sendMail(@CookieValue(name = "googleAccess", required = false) String googleAccessToken,
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
        
        if (googleAccessToken != null) {
            try {
                // PDF 생성
                String fileId = pdfService.createPDF(custMap, pdfMap, mailMap, googleAccessToken);

                // 로그인된 구글 ID로 고객에게 메일 전송
                googleService.sendEmail(googleAccessToken, "pdfUrl", custMap, pdfMap, mailMap,fileId);

                // 정상적으로 처리된 경우 메시지 반환
                
                return ResponseEntity.status(HttpStatus.OK).body("메일이 정상적으로 발송되었습니다.");
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("메일 전송 중 오류가 발생했습니다.");
            }
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Google Access Token이 필요합니다.");
    }
    
    @GetMapping("/pdfAsImage")
    @Operation(summary = "Pdf파일을 Image로 변환하여 Return", description = "")
    public ResponseEntity<List<String>> pdfAsImage(@RequestParam String pdfPath){
    	//https://drive.google.com/file/d/1_o3eB91MeiQR5jIY_8MpGc5hEKZMwLpH/view?usp=drive_link
    	try {
    		
    		System.out.println("pdfPath===>"+pdfPath);
    		 // 파일 ID 추출
    		String fileId = pdfPath;


            // PDF 다운로드
            File pdfFile = pdfService.downloadPdfFromLink(fileId);

            // PDF를 Json으로 변환
            List<String> imageJson = pdfService.convertPdfToBase64Images(pdfFile);

            // 결과 이미지 경로 반환

            return ResponseEntity.ok(imageJson);
    	} catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
}
