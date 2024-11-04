package com.eSignify.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eSignify.common.CommonDao;
@Service
public class LoginService {
	
	@Autowired 
	CommonDao commonDao;
	
	public int loginChk(Map<String, Object> multiList){
		
		System.out.println(multiList.toString());
		
		Object result = commonDao.selectOne("selectUser", multiList); // CommonDao를 통해 쿼리 실행
		
		
		return 1;
		
	}

}
