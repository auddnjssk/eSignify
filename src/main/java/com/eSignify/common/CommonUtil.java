package com.eSignify.common;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


@Component
public class CommonUtil {

	@Value("${SUPABASE_URL}")
	private String SUPABASE_URL;
	
	@Value("${SUPABASE_KEY}")
	private String SUPABASE_KEY;
	
	// 랜덤으로 비밀 키 생성 (32 바이트, 256비트)
    public String generateSecretKey() {
        byte[] key = new byte[32];
        SecureRandom random = new SecureRandom();
        random.nextBytes(key);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(key);
    }
    
    public String comEncode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    public String comDecode(String input) {
        byte[] decodedBytes = Base64.getDecoder().decode(input);
        return new String(decodedBytes);
    }

    public Response supaBaseSelect(String action , String tableName,String condition) {
    	
    	String urlAdd = "/rest/v1/" + tableName + "?"+condition;
    	
    	String urlAddEn = comEncode(urlAdd) ;
    	
    	System.out.println("urlAdd==>"+urlAdd);
    	
        OkHttpClient client = new OkHttpClient();

        // Supabase REST API 호출 URL
        String url = SUPABASE_URL +comDecode(urlAddEn);
        
        System.out.println("SUPABASE_URL==>"+SUPABASE_URL);
        
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
                String responseData = response.body().string();
                System.out.println("조회된 데이터: " + responseData);
                return response;
            } else {
                // 요청 실패 시 오류 코드 및 메시지 출력
                System.err.println("오류 발생: " + response.code() + " - " + response.message());
                return response;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
