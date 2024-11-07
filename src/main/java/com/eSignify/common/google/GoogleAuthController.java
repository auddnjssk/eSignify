package com.eSignify.common.google;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GoogleAuthController {
	
	@Autowired
	GoogleAuthService googleAuthService;

	@Autowired
	GoogleFnService googleFnService;
	

	
    // 확인하고 계속하기
    @GetMapping("/oauth/google")
    public String googleRedirect(@RequestParam String code) throws Exception {
    	System.out.println("!!");
    	String accessToken = googleAuthService.getAccessToken(code);
    	
    	googleFnService.sendEmail(accessToken , "" , "테스트" , "보내지나 ?");
    	
        return "123";
    }
}
