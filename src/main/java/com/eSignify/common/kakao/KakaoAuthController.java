package com.eSignify.common.kakao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KakaoAuthController {

    @Autowired
    private KakaoAuthService kakaoAuthService;
    
    @Autowired
    private KakaoSendService kakaoSendService;

    
    // Ȯ���ϰ� ����ϱ�
    // https://kauth.kakao.com/oauth/authorize?client_id=afd0afadcf81f1efaf97fe02625c2963&redirect_uri=http://localhost:8080/oauth/kakao&response_type=code
    @GetMapping("/oauth/kakao")
    public AccessTokenResponse kakaoCallback(@RequestParam String code) throws Exception {
    	
        // authorization_code�� ����� accessToken�� �߱޹޽��ϴ�.
    	
    	AccessTokenResponse	tkResponse = kakaoAuthService.getAccessToken(code);
    	
        kakaoSendService.getFriends(tkResponse);
        
        
        return tkResponse;
    }
}