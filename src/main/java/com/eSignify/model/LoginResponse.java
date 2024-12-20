package com.eSignify.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {

    @JsonProperty("USER_ID")
    private String userId;

    @JsonProperty("USER_PASSWORD")
    private String userPassword;

    @JsonProperty("USER_POSITION")
    private String userPosition;

    @JsonProperty("USER_BUSI_NM")
    private String userBusinessName;

    @JsonProperty("USER_NM")
    private String userName;

    @JsonProperty("KAKAO_ID")
    private Long kakaoId;

    @JsonProperty("KAKAO_EMAIL")
    private String kakaoEmail;

    @JsonProperty("GOOGLE_ID")
    private String googleId;
}
