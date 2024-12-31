package com.eSignify.kakao.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.eSignify.common.CommonUtil;
import com.eSignify.kakao.service.KakaoAuthService;
import com.eSignify.kakao.service.KakaoSendService;
import com.eSignify.model.AccessTokenResponse;
import com.eSignify.model.KakaoUserDTO;
import com.eSignify.model.LoginResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import okhttp3.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.List;


@RestController
@CrossOrigin(origins = "http://localhost:8080") // ����Ʈ���� �ּ� ���
public class KakaoController {

    @Autowired
    private KakaoAuthService kakaoAuthService;
    
    @Autowired
    private KakaoSendService kakaoSendService;
    
	@Autowired 
	private CommonUtil comUtil; 

    
    @PostMapping("/oauth/kakao")
    public AccessTokenResponse kakaoCallback(@RequestParam String code) throws Exception {
    	
    	AccessTokenResponse	accessToken = kakaoAuthService.getAccessToken(code);
    	
        return accessToken;
    }
    
    @PostMapping("/kakao/connect")
    public ResponseEntity<String> kakaoConnect(@RequestBody KakaoUserDTO kakaoUserDTO,HttpServletRequest request) throws Exception {
    	try {
    		
            String condition = "KAKAO_EMAIL=eq."+kakaoUserDTO.getEmail();
            
            String tableName = "T_USER";
            HttpSession session = request.getSession();
            String userId = (String) session.getAttribute("userId");
            
            List<LoginResponse> selectResponse= comUtil.supaBaseSelect("", tableName,condition);
            
    		
            if (selectResponse != null) {
                return ResponseEntity.ok("īī�� ������ ���������� �����Ǿ����ϴ�.");
            } else {
            	condition = "USER_ID=eq."+userId;
            			
            	String body = "{\"KAKAO_ID\": \"" + kakaoUserDTO.getKakaoId() + "\", \"KAKAO_EMAIL\": \"" + kakaoUserDTO.getEmail() + "\"}";
            	
            	ResponseEntity<String> response =comUtil.supaBaseUpdate(tableName,condition,body);
            	
            	System.out.println(response.getStatusCode());
            	
                return ResponseEntity.ok("���ο� ������ �����ǰ� īī�� ������ �����Ǿ����ϴ�.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("���� ���� �� ������ �߻��߽��ϴ�.");
        }
    }
    
    
    @PostMapping("/send-message")
    // ��Ű�� �޾ƿ�������
    @CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
    public ResponseEntity<String> sendMessage(HttpServletRequest request) {
        String accessToken = null;
        
        // ��Ű���� īī�� Access Token ��������
        jakarta.servlet.http.Cookie[] cookies = request.getCookies(); // jakarta.servlet.http.Cookie ���
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("kakaoAccessToken".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                    break; // ��ū�� ã�����Ƿ� �ݺ��� Ż��
                }
            }
        }

        // Access Token�� ���� ��� ó��
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access token not found");
        }
        
        
        kakaoSendService.sendMessage2Me(accessToken);
        
        return ResponseEntity.status(HttpStatus.OK).body("Access token not found");
        
    }
}
