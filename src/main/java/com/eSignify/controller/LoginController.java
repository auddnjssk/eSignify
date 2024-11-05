package com.eSignify.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.eSignify.service.LoginService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;


@Controller
public class LoginController {

	@Autowired 
	private LoginService loginService; 

	@GetMapping("/loginChk")
	public String loginChk() {
		

		return "index";
	}
    
 	@PostMapping("/loginChk")
 	public void loginChk(HttpServletRequest request,
 			@RequestBody List<Map<String, Object>> multiList, HttpSession session) throws Exception {
 		loginService.loginChk(multiList.get(0));
 	}
 	
 	
}
