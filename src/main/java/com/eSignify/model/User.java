package com.eSignify.model;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class User {

	@Column(name="USER_CNT") // 유저 체크 cnt
	private String userCnt;

}
