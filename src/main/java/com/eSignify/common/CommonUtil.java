package com.eSignify.common;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.eSignify.model.LoginResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
public class CommonUtil {

	@Value("${SUPABASE_URL}")
	private String SUPABASE_URL;
	
	@Value("${SUPABASE_KEY}")
	private String SUPABASE_KEY;
	
	private final OkHttpClient client = new OkHttpClient();

	// 랜덤으로 비밀 키 생성 (32 바이트, 256비트)
    public String generateSecretKey() {
        byte[] key = new byte[32];
        SecureRandom random = new SecureRandom();
        random.nextBytes(key);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(key);
    }
    
    // Encoding
    public String comEncode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    // Decoding
    public String comDecode(String input) {
        byte[] decodedBytes = Base64.getDecoder().decode(input);
        return new String(decodedBytes);
    }

    // SupaBase Select 메소드
    public List<Map<String,Object>> supaBaseSelect(String tableName,String condition){
    	
    	String responseData;
    	
    	// Jackson ObjectMapper 생성
        ObjectMapper objectMapper = new ObjectMapper();

        // JSON을 Get하기위해 객체로 변환
        List<Map<String, Object>> ListResponse ;
        
    	String urlAdd = "/rest/v1/" + tableName + "?"+condition;
    	
    	String urlAddEn = comEncode(urlAdd) ;
    	
        OkHttpClient client = new OkHttpClient();

        // Supabase REST API 호출 URL
        String url = SUPABASE_URL +comDecode(urlAddEn);
        
        // HTTP 요청 생성
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", SUPABASE_KEY)  // Supabase API 키 설정
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)  // 인증 헤더 설정
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                // 요청 성공 시 응답 데이터 출력
                responseData = response.body().string();
                ListResponse = objectMapper.readValue(responseData, new TypeReference<List<Map<String,Object>>>() {});
                
                return ListResponse;
            } else {
                // 요청 실패 시 오류 코드 및 메시지 출력
            	responseData = "오류 발생: " + response.code() + " - " + response.message();
            	ListResponse = objectMapper.readValue(responseData, new TypeReference<List<Map<String,Object>>>() {});
                return ListResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // SupaBase Update 메소드
    public ResponseEntity<String> supaBaseUpdate(String tableName,String condition,String body) {
    	

    	String url = SUPABASE_URL +"/rest/v1/" + tableName + "?"+condition;

  
        RequestBody jsonBody = RequestBody.create(body, MediaType.get("application/json; charset=utf-8"));
        
        Request request = new Request.Builder()
                .url(url)
                .patch(jsonBody)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return ResponseEntity.ok("Supabase 업데이트 성공");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Supabase 업데이트 실패: " + response.message());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Supabase 요청 중 오류 발생: " + e.getMessage());
        }
    }
    
    // SupaBase insert 메소드
    public ResponseEntity<String> supaBaseInsert (String tableName,JsonObject body) {
    	
    	String url = SUPABASE_URL +"/rest/v1/" + tableName ;
    	
    	RequestBody jsonBody = RequestBody.create(body.toString(), MediaType.get("application/json; charset=utf-8"));
    	
    	Request request = new Request.Builder()
    			.url(url)
    			.post(jsonBody)
    			.addHeader("apikey", SUPABASE_KEY)
    			.addHeader("Authorization", "Bearer " + SUPABASE_KEY)
    			.addHeader("Content-Type", "application/json")
    			.build();
    	
    	try (Response response = client.newCall(request).execute()) {
    		if (response.isSuccessful()) {
    			return ResponseEntity.ok("Supabase 업데이트 성공");
    		} else {
    			System.out.println(("Supabase 업데이트 실패: " + response.message()));
    			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Supabase 업데이트 실패: " + response.message());
    		}
    	} catch (Exception e) {
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Supabase 요청 중 오류 발생: " + e.getMessage());
    	}
    }
    
    // SupaBase delete 메소드
    public ResponseEntity<String> supaBaseDelete(String tableName,String condition) {
    	
    	String url = SUPABASE_URL +"/rest/v1/" + tableName +"?"+condition;
    	
    	Request request = new Request.Builder()
    			.url(url)
    			.delete()
    			.addHeader("apikey", SUPABASE_KEY)
    			.addHeader("Authorization", "Bearer " + SUPABASE_KEY)
    			.addHeader("Content-Type", "application/json")
    			.build();
    	
    	try (Response response = client.newCall(request).execute()) {
    		if (response.isSuccessful()) {
    			return ResponseEntity.ok("Supabase 삭제 성공");
    		} else {
    			System.out.println(("Supabase 삭제 실패: " + response.message()));
    			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Supabase 삭제 실패: " + response.message());
    		}
    	} catch (Exception e) {
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Supabase 요청 중 오류 발생: " + e.getMessage());
    	}
    }

}
