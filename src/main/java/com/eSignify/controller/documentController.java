package com.eSignify.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.eSignify.common.CommonUtil;

@Controller
public class documentController {
	
	@Autowired
	CommonUtil commonUtil;
	
	
	@PostMapping(value = "/docuTest", consumes = {"application/json", "text/plain"})
	public void docuTest(@RequestBody Map<String, String> requestBody) {
		
		//commonUtil.createPDF(requestBody);
		//commonUtil.createPDF(requestBody);
		
	}
	

}
