package eSignify.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import eSignify.service.LoginService;

@Controller
public class LoginController {

	//@Autowired private LoginService service; 

	@PostMapping("/loginChk")
	public String loginChk(RequestBody request) {
		System.out.println("메인 페이지 진입");
		

		return "index";
	}
}
