package com.eSignify.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginMapper {
	
	int loginCheck();

}
