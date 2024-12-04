package com.eSignify.common.kakao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.eSignify.common.CommonUtil;
import com.eSignify.common.kakao.service.KakaoAuthService;
import com.eSignify.common.kakao.service.KakaoSendService;
import com.eSignify.model.KakaoUserDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import okhttp3.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;


@RestController
@CrossOrigin(origins = "http://localhost:8080") // ����Ʈ���� �ּ� ���
public class KakaoController {

    @Autowired
    private KakaoAuthService kakaoAuthService;
    
    @Autowired
    private KakaoSendService kakaoSendService;
    
	@Autowired 
	private CommonUtil comUtil; 

    
	// �����̷�Ʈ URL
    @PostMapping("/oauth/kakao")
    public AccessTokenResponse kakaoCallback(@RequestParam String code) throws Exception {
    	
    	AccessTokenResponse	accessToken = kakaoAuthService.getAccessToken(code);
    	
        //kakaoSendService.getFriends(tkResponse);
    	// ��Ű�� Access Token ����
//        jakarta.servlet.http.Cookie kakaoCookie = new jakarta.servlet.http.Cookie("kakaoAccessToken", accessToken);
//        kakaoCookie.setHttpOnly(true); // Ŭ���̾�Ʈ���� ���� �Ұ����ϰ� �����Ͽ� ���ȼ� ��ȭ
//        kakaoCookie.setSecure(true); // HTTPS�θ� ���۵ǵ��� ����
//        kakaoCookie.setPath("/"); // ��� ��ο��� ��Ű ���� ����
//        kakaoCookie.setMaxAge(60 * 60); // ���� �ð� ���� (��: 1�ð� ���� ��ȿ)
//        
        
        return accessToken;
    }
    
    // Ȯ���ϰ� ����ϱ�
    @PostMapping("/kakao/connect")
    public ResponseEntity<String> kakaoConnect(@RequestBody KakaoUserDTO kakaoUserDTO,HttpServletRequest request) throws Exception {
    	try {
            // ����� ������ DB�� �����ϴ��� Ȯ��
    		
            String condition = "KAKAO_EMAIL=eq."+kakaoUserDTO.getEmail();
            
            String tableName = "T_USER";
            HttpSession session = request.getSession();
            String userId = (String) session.getAttribute("userId");
            
    		Response selectResponse= comUtil.supaBaseSelect("", tableName,condition);
            
    		
            if (selectResponse.request().body() != null) {
//                // ���� ����� ������ īī�� ���� ����
//                existingUser.setKakaoId(kakaoUserDTO.getKakaoId());
//                userService.save(existingUser);
                return ResponseEntity.ok("īī�� ������ ���������� �����Ǿ����ϴ�.");
            } else {
                // ����ڰ� ���ٸ� ���ο� ���� ����
//                User newUser = new User();
//                newUser.setEmail(kakaoUserDTO.getEmail());
//                newUser.setNickname(kakaoUserDTO.getNickname());
//                newUser.setKakaoId(kakaoUserDTO.getKakaoId());
//                userService.save(newUser);
            	
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
//http://localhost:8080/oauth/google
//https://accounts.google.com/o/oauth2/v2/auth?client_id=298618192214-r7d4c3p7jaavis7nghdrbug1bs0shkoh.apps.googleusercontent.com&redirect_uri=http://localhost:8080/oauth/google&response_type=code&scope=email%20profile&state=YOUR_UNIQUE_STATE
