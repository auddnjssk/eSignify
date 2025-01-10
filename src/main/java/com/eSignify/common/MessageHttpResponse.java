package com.eSignify.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class MessageHttpResponse {

    // 성공적인 응답을 반환하는 메서드
    public ResponseEntity<?> success(ResponseDTO messageDto) {
        return new ResponseEntity<>(messageDto, HttpStatus.OK); // 200 OK 응답
    }

    // 실패한 응답을 반환하는 메서드
    public ResponseEntity<?> failure(ResponseDTO messageDto) {
        return new ResponseEntity<>(messageDto, HttpStatus.BAD_REQUEST); // 400 Bad Request 응답
    }

    // 기타 상태 코드에 맞는 응답을 반환할 수 있는 메서드
    public ResponseEntity<?> customResponse(ResponseDTO messageDto, HttpStatus status) {
        return new ResponseEntity<>(messageDto, status); // 사용자 정의 상태 코드 응답
    }
}
