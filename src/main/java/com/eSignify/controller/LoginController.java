package com.eSignify.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.eSignify.common.CommonUtil;
import com.eSignify.common.ObjectUtil;
import com.eSignify.kakao.service.KakaoSendService;
import com.eSignify.model.LoginResponse;
import com.eSignify.service.LoginService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;


@Controller
public class LoginController {

	@Autowired 
	private LoginService loginService; 
	
	@Autowired 
	private KakaoSendService kakaoSendService; 
	
	@Autowired 
	private CommonUtil comUtil; 
	
    private static final long EXPIRATION_TIME = 864_000_00; // 1�씪 (諛�由ъ큹)

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> requestBody,HttpServletRequest request) throws IOException {
    	
        String userId = requestBody.get("userId");
        String userPassword = requestBody.get("passWord");
        String condition = "USER_ID=eq."+userId + "&USER_PASSWORD=eq." + userPassword;
        String tableName = "T_USER";
        
        String jwtToken ;
        
        try {
        	// DB
        	List<LoginResponse> selectResponse= comUtil.supaBaseSelect(userId, tableName,condition);
        	
        	// 회원가입 안되어있으면 return
            if(selectResponse.size() <= 0) {

            	jwtToken = null;

            }else {
            	
                String SECRET_KEY = comUtil.generateSecretKey();
            	
                long currentTimeMillis = System.currentTimeMillis();
                Date now = new Date(currentTimeMillis);
                Date expiryDate = new Date(currentTimeMillis + EXPIRATION_TIME);

                jwtToken = Jwts.builder()
                        .setSubject(userId)
                        .setIssuedAt(now)
                        .setExpiration(expiryDate)
                        .signWith(SignatureAlgorithm.HS256, SECRET_KEY) 
                        .compact();
                
                //카카오 ID가 있다면 ACCESS토큰을 가져와서 쿠키에 뿌림
                if(ObjectUtil.isNotEmpty(selectResponse.get(0).getKakaoId())) {
                	
                }
                //구글 ID가 있다면 ACCESS토큰을 가져와서 쿠키에 뿌림
                if(ObjectUtil.isNotEmpty(selectResponse.get(0).getGoogleId())) {
                	
                }
                
                // Session에 UserId 저장
                HttpSession session = request.getSession();
                session.setAttribute("userId", userId);
            	
            }
            
            return ResponseEntity.ok(jwtToken);
            
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
    
 	@PostMapping("/loginChk")
 	public void loginChk(HttpServletRequest request,
 			@RequestBody List<Map<String, Object>> multiList, HttpSession session) throws Exception {
 		loginService.loginChk(multiList.get(0));
 	}
 	
    @GetMapping("/friends")
    public String getFriends(@RequestParam String accessToken) {
    	//kakaoSendService.getFriends(accessToken);
        return "123";
    }

 	
}
