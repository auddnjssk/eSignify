package com.eSignify.mapper.loginMapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginMapper {
	
	int loginCheck();

}
