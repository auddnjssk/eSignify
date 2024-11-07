package com.eSignify.common.google;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GoogleAuthController {
	
	@Autowired
	GoogleAuthService googleAuthService;

	@Autowired
	GoogleFnService googleFnService;
	

//	https://accounts.google.com/o/oauth2/v2/auth?
//		  scope=openid%20email%20profile%20https://www.googleapis.com/auth/gmail.readonly%20https://www.googleapis.com/auth/gmail.send&
//		  access_type=offline&
//		  response_type=code&
//		  redirect_uri=http://localhost:8080/oauth/google&
//		  client_id=298618192214-r7d4c3p7jaavis7nghdrbug1bs0shkoh.apps.googleusercontent.com&
//		  state=RANDOM_STATE_VALUE
//    // Ȯ���ϰ� ����ϱ�
    @GetMapping("/oauth/google")
    public ResponseEntity<String> googleRedirect(@RequestParam String code) throws Exception {
    	System.out.println("!!" + code);
    	
    	ResponseEntity<String> authorization = googleAuthService.getAccessToken(code);
    	
    	//googleFnService.sendEmail(accessToken , "" , "테스트" , "보내지나?");
    	
        return authorization;
    }
}
