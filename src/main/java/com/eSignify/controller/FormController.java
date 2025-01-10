package com.eSignify.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.eSignify.common.CommonUtil;
import com.eSignify.common.MessageHttpResponse;
import com.eSignify.common.ResponseDTO;
import com.eSignify.common.utils.Constants;
import com.eSignify.model.KakaoUserDTO;
import com.google.gson.JsonObject;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class FormController {
	
	@Autowired
	CommonUtil commonUtil;
	
	@Autowired
	MessageHttpResponse messageHttpResponse;
	
	@GetMapping("/formManagement")
	public ResponseEntity<?> readForm (@CookieValue(name = "userId") String userId , 
		HttpServletResponse response,HttpServletRequest request,HttpSession session){
		
		String condition = "user_id=eq."+userId;
		
		List<Map<String, Object>> selectList = commonUtil.supaBaseSelect("t_form",condition );
		
        ResponseDTO responseDTO = new ResponseDTO.Builder()
                .setMessage(Constants.normalMessage)
                .setResult(selectList)
                .setStatusCode(200)
                .build();
        
        return messageHttpResponse.success(responseDTO);
	}
	
	@PostMapping("/formManagement")
	public ResponseEntity<?> createForm (@CookieValue(name = "userId") String userId , @RequestBody Map<String,Object> requestBody , 
			HttpServletResponse response,HttpServletRequest request,HttpSession session){
		
		System.out.println("userId" + requestBody.toString());
		
        JsonObject supaBaseBody = new JsonObject();
        supaBaseBody.addProperty("form_title",(String) requestBody.get("form_title"));
        supaBaseBody.addProperty("form_detail",(String) requestBody.get("form_detail"));
        supaBaseBody.addProperty("user_id",userId);
		
        ResponseEntity<String> selectList = commonUtil.supaBaseInsert("t_form",supaBaseBody );
		 
		ResponseDTO responseDTO = new ResponseDTO.Builder()
				.setMessage(Constants.normalMessage)
				.setResult(selectList)
				.setStatusCode(200)
				.build();
		
		return messageHttpResponse.success(responseDTO);
	}
	

}
