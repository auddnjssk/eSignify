package com.eSignify.common;

import lombok.Getter;

@Getter
public class ResponseDTO {

    private String message;
    private Object result;
    private int statusCode;
    private String accessToken;  // jwtToken 필드 추가

    // 기존 생성자에 jwtToken 추가
    public ResponseDTO(String message, Object result, int statusCode, String jwtToken) {
        this.message = message;
        this.result = result;
        this.statusCode = statusCode;
        this.accessToken = jwtToken;  // jwtToken 값 설정
    }

    public static class Builder {
        private String message;
        private Object result;
        private int statusCode;
        private String accessToken;  // jwtToken 설정을 위한 필드 추가

        // 메시지 설정
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        // 결과 설정
        public Builder setResult(Object result) {
            this.result = result;
            return this;
        }

        // 상태 코드 설정
        public Builder setStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        // jwtToken 설정
        public Builder setJwtToken(String accessToken) {
            this.accessToken = accessToken;  // jwtToken 값을 설정
            return this;
        }

        // ResponseDTO 객체 생성
        public ResponseDTO build() {
            return new ResponseDTO(message, result, statusCode, accessToken);  // 생성자에 jwtToken 추가
        }
    }
}
