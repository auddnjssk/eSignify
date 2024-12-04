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
@CrossOrigin(origins = "http://localhost:8080") // 프론트엔드 주소 명시
public class KakaoController {

    @Autowired
    private KakaoAuthService kakaoAuthService;
    
    @Autowired
    private KakaoSendService kakaoSendService;
    
	@Autowired 
	private CommonUtil comUtil; 

    
	// 리다이렉트 URL
    @PostMapping("/oauth/kakao")
    public AccessTokenResponse kakaoCallback(@RequestParam String code) throws Exception {
    	
    	AccessTokenResponse	accessToken = kakaoAuthService.getAccessToken(code);
    	
        //kakaoSendService.getFriends(tkResponse);
    	// 쿠키에 Access Token 저장
//        jakarta.servlet.http.Cookie kakaoCookie = new jakarta.servlet.http.Cookie("kakaoAccessToken", accessToken);
//        kakaoCookie.setHttpOnly(true); // 클라이언트에서 접근 불가능하게 설정하여 보안성 강화
//        kakaoCookie.setSecure(true); // HTTPS로만 전송되도록 설정
//        kakaoCookie.setPath("/"); // 모든 경로에서 쿠키 접근 가능
//        kakaoCookie.setMaxAge(60 * 60); // 만료 시간 설정 (예: 1시간 동안 유효)
//        
        
        return accessToken;
    }
    
    // 확인하고 계속하기
    @PostMapping("/kakao/connect")
    public ResponseEntity<String> kakaoConnect(@RequestBody KakaoUserDTO kakaoUserDTO,HttpServletRequest request) throws Exception {
    	try {
            // 사용자 정보가 DB에 존재하는지 확인
    		
            String condition = "KAKAO_EMAIL=eq."+kakaoUserDTO.getEmail();
            
            String tableName = "T_USER";
            HttpSession session = request.getSession();
            String userId = (String) session.getAttribute("userId");
            
    		Response selectResponse= comUtil.supaBaseSelect("", tableName,condition);
            
    		
            if (selectResponse.request().body() != null) {
//                // 기존 사용자 계정과 카카오 계정 연결
//                existingUser.setKakaoId(kakaoUserDTO.getKakaoId());
//                userService.save(existingUser);
                return ResponseEntity.ok("카카오 계정이 성공적으로 연동되었습니다.");
            } else {
                // 사용자가 없다면 새로운 계정 생성
//                User newUser = new User();
//                newUser.setEmail(kakaoUserDTO.getEmail());
//                newUser.setNickname(kakaoUserDTO.getNickname());
//                newUser.setKakaoId(kakaoUserDTO.getKakaoId());
//                userService.save(newUser);
            	
            	condition = "USER_ID=eq."+userId;
            			
            	String body = "{\"KAKAO_ID\": \"" + kakaoUserDTO.getKakaoId() + "\", \"KAKAO_EMAIL\": \"" + kakaoUserDTO.getEmail() + "\"}";
            	
            	ResponseEntity<String> response =comUtil.supaBaseUpdate(tableName,condition,body);
            	
            	System.out.println(response.getStatusCode());
            	
                return ResponseEntity.ok("새로운 계정이 생성되고 카카오 계정이 연동되었습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("계정 연동 중 오류가 발생했습니다.");
        }
    }
    
    
    @PostMapping("/send-message")
    // 쿠키를 받아오기위함
    @CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
    public ResponseEntity<String> sendMessage(HttpServletRequest request) {
        String accessToken = null;
        
        // 쿠키에서 카카오 Access Token 가져오기
        jakarta.servlet.http.Cookie[] cookies = request.getCookies(); // jakarta.servlet.http.Cookie 사용
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("kakaoAccessToken".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                    break; // 토큰을 찾았으므로 반복문 탈출
                }
            }
        }

        // Access Token이 없을 경우 처리
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access token not found");
        }
        
        
        kakaoSendService.sendMessage2Me(accessToken);
        
        return ResponseEntity.status(HttpStatus.OK).body("Access token not found");
        
    }
}
//http://localhost:8080/oauth/google
//https://accounts.google.com/o/oauth2/v2/auth?client_id=298618192214-r7d4c3p7jaavis7nghdrbug1bs0shkoh.apps.googleusercontent.com&redirect_uri=http://localhost:8080/oauth/google&response_type=code&scope=email%20profile&state=YOUR_UNIQUE_STATE
