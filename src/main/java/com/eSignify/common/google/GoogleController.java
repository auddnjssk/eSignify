package com.eSignify.common.google;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eSignify.model.KakaoUserDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
public class GoogleController {
	
	@Autowired
	GoogleService googleAuthService;

	@Autowired
	GoogleFnService googleFnService;
	

//	https://accounts.google.com/o/oauth2/v2/auth?
//		  scope=openid%20email%20profile%20https://www.googleapis.com/auth/gmail.readonly%20https://www.googleapis.com/auth/gmail.send&
//		  access_type=offline&
//		  response_type=code&
//		  redirect_uri=http://localhost:8080/oauth/google&
//		  client_id=298618192214-r7d4c3p7jaavis7nghdrbug1bs0shkoh.apps.googleusercontent.com&
//		  state=RANDOM_STATE_VALUE
//    // Ȯ���ϰ� ����ϱ�?
    @GetMapping("/oauth/google")
    public ResponseEntity<String> googleRedirect(@RequestParam String code) throws Exception {
    	System.out.println("!!" + code);
    	
    	ResponseEntity<String> authorization = googleAuthService.getAccessToken(code);
    	
        // ��Ű�� Access Token ����
        jakarta.servlet.http.Cookie googleCookie = new jakarta.servlet.http.Cookie("kakaoAccessToken", code);
        googleCookie.setHttpOnly(true); // Ŭ���̾�Ʈ���� ���� �Ұ����ϰ� �����Ͽ� ���ȼ� ��ȭ
        googleCookie.setSecure(true); // HTTPS�θ� ���۵ǵ��� ����
        googleCookie.setPath("/"); // ��� ��ο��� ��Ű ���� ����
        googleCookie.setMaxAge(60 * 60); // ���� �ð� ���� (��: 1�ð� ���� ��ȿ)
    	
    	//googleFnService.sendEmail(accessToken , "" , "?��?��?��" , "보내�??��?");
    	
        return authorization;
    }
    
    @PostMapping("/google/sendMail")
    public void googleSendMail(@RequestBody KakaoUserDTO kakaoUserDTO,HttpServletRequest request) throws Exception {

    	HttpSession session = request.getSession();
        String accessToken = (String) session.getAttribute("accessToken");
    	
    	googleFnService.sendEmail(accessToken , "" , "?��?��?��" , "보내�??��?");
    	
    }
}
