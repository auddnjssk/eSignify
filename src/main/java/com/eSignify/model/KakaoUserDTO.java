package com.eSignify.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoUserDTO {
    private Long kakaoId;
    private String email;
    private String nickname;

}
