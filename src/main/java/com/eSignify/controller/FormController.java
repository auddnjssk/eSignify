package com.eSignify.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.eSignify.common.CommonUtil;
import com.eSignify.common.MessageHttpResponse;
import com.eSignify.common.ObjectUtil;
import com.eSignify.common.ResponseDTO;
import com.eSignify.common.utils.Constants;
import com.eSignify.common.utils.JwtTokenUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	
	@Autowired 
	private JwtTokenUtil jwtTokenUtil; 
	
	@GetMapping("/formManagement")
	public ResponseEntity<?> readForm (@RequestHeader("Authorization") String authorizationHeader , 
		HttpServletResponse response,HttpServletRequest request,HttpSession session){
		
		String token = authorizationHeader.replace("Bearer ", "");

		ObjectMapper ob = new ObjectMapper();
		
		try {
			JsonNode jn = ob.readTree(token);
			token = jn.get("value").asText();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String userId = jwtTokenUtil.getUserIdFromToken(token);
		
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
	public ResponseEntity<?> createForm (@RequestHeader("Authorization") String authorizationHeader ,@RequestBody Map<String,Object> requestBody , 
			HttpServletResponse response,HttpServletRequest request,HttpSession session){
		
		String token = authorizationHeader.replace("Bearer ", "");

		ObjectMapper ob = new ObjectMapper();
		
		try {
			JsonNode jn = ob.readTree(token);
			token = jn.get("value").asText();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<Map<String,Object>> seq = commonUtil.supaBaseSelect("form_no_seq","");
		
		int formId = (int) seq.get(0).get("last_value");
		
		
		String userId = jwtTokenUtil.getUserIdFromToken(token);
		
		// form Info Insert
        JsonObject supaBaseBody = new JsonObject();
        supaBaseBody.addProperty("form_title",(String) requestBody.get("form_title"));
        supaBaseBody.addProperty("user_id",userId);
		
        commonUtil.supaBaseInsert("t_form",supaBaseBody );
		 
        // file Info Insert
        supaBaseBody = new JsonObject();
        supaBaseBody.addProperty("form_title",(String) requestBody.get("form_title"));
        supaBaseBody.addProperty("user_id",userId);
        
        ResponseEntity<String> selectList = commonUtil.supaBaseInsert("t_form",supaBaseBody );
        
        
		ResponseDTO responseDTO = new ResponseDTO.Builder()
				.setMessage(Constants.normalMessage)
				.setResult(formId)
				.setStatusCode(200)
				.build();
		
		return messageHttpResponse.success(responseDTO);
	}
	
	@GetMapping("/formCoordinate")
	public ResponseEntity<?> getCoordinate (@RequestHeader("Authorization") String authorizationHeader ,@RequestParam("formId") String formId, 
			HttpServletResponse response,HttpServletRequest request,HttpSession session){
		
		String cond = "form_id=eq."+formId;
		
		List<Map<String,Object>> selectList = commonUtil.supaBaseSelect("t_pdf_coordinate",cond);
		
		
		ResponseDTO responseDTO = new ResponseDTO.Builder()
				.setMessage(Constants.normalMessage)
				.setResult(selectList)
				.setStatusCode(200)
				.build();
		
		return messageHttpResponse.success(responseDTO);
	}
	
	@PostMapping("/formCoordinate")
	public ResponseEntity<?> createCoordinate (@RequestHeader("Authorization") String authorizationHeader ,@RequestBody Map<String,Object> requestBody , 
			HttpServletResponse response,HttpServletRequest request,HttpSession session){
		
		String token = authorizationHeader.replace("Bearer ", "");
		
		ObjectMapper ob = new ObjectMapper();
		
		try {
			JsonNode jn = ob.readTree(token);
			token = jn.get("value").asText();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String formId = (String) requestBody.get("formId");
		
		
		String userId = jwtTokenUtil.getUserIdFromToken(token);

		String deleteCond = "form_id=eq."+formId;
				
		commonUtil.supaBaseDelete("t_pdf_coordinate",deleteCond);
		
		
		@SuppressWarnings("unchecked")
		List<ArrayList<?>> arrayList = (ArrayList<ArrayList<?>>) requestBody.get("inputFields");
		
		JsonObject supaBaseBody = new JsonObject();
		
		for(int i=0 ; arrayList.size() > i ; i++) {
			int pageNum = 1+i;
			supaBaseBody.addProperty("page_number",pageNum);
			supaBaseBody.addProperty("form_id",formId);

			if(ObjectUtil.isEmpty(arrayList.get(i))) continue;
			
			for( Object map : arrayList.get(i) ) {
				Map<String, Object> obMap  =  ObjectUtil.converObjectToMap(map);
				supaBaseBody.addProperty("x_coord", obMap.get("top").toString());
				supaBaseBody.addProperty("y_coord",(String) obMap.get("left").toString());
				supaBaseBody.addProperty("x_relative",(String) obMap.get("relativeX").toString());
				supaBaseBody.addProperty("y_relative",(String) obMap.get("relativeY").toString());
				supaBaseBody.addProperty("width_relative",(String) obMap.get("relativeWidth").toString());
				supaBaseBody.addProperty("height_relative",(String) obMap.get("relativeHeight").toString());
				supaBaseBody.addProperty("input_type",(String) obMap.get("inputType").toString());
				commonUtil.supaBaseInsert("t_pdf_coordinate",supaBaseBody);
			}
			
		}
		
		ResponseDTO responseDTO = new ResponseDTO.Builder()
				.setMessage(Constants.normalMessage)
				.setResult(formId)
				.setStatusCode(200)
				.build();
		
		return messageHttpResponse.success(responseDTO);
	}
	

}
