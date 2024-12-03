package com.eSignify.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.eSignify.common.kakao.service.KakaoSendService;
import com.eSignify.service.LoginService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;


@Controller
public class LoginController {

	@Autowired 
	private LoginService loginService; 
	
	@Autowired 
	private KakaoSendService kakaoSendService; 
	
	@Autowired
    private AuthenticationManager authenticationManager;
	

	@PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> credentials) {
		
        String username = credentials.get("userId");
        String password = credentials.get("passWord");

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            return ResponseEntity.ok("Login successful");
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
    
    // 친구에게 메시지 전송 API
    @GetMapping("/sendMessage")
    public String sendMessage(@RequestParam String accessToken, @RequestParam String friendId, @RequestParam String messageText) {
        return kakaoSendService.sendMessage(accessToken, friendId, messageText);
    }
 	
}
