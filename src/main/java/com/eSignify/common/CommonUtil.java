package com.eSignify.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.eSignify.model.LoginResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.Session;
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
    public List<LoginResponse> supaBaseSelect(String action , String tableName,String condition){
    	
    	String responseData;
    	
    	// Jackson ObjectMapper 생성
        ObjectMapper objectMapper = new ObjectMapper();

        // JSON을 Get하기위해 객체로 변환
        List<LoginResponse> jsonResponse ;
        
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
                jsonResponse = objectMapper.readValue(responseData, new TypeReference<List<LoginResponse>>() {});
                
                return jsonResponse;
            } else {
                // 요청 실패 시 오류 코드 및 메시지 출력
            	responseData = "오류 발생: " + response.code() + " - " + response.message();
            	jsonResponse = objectMapper.readValue(responseData, new TypeReference<List<LoginResponse>>() {});
                return jsonResponse;
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
    
    // OpenHTMLToPDF로 PDF 변환 메소드
    public String createPDF(Map<String, String> requestBody,HttpServletRequest request) {
    	
    	String CUST_CD = requestBody.get("CUST_CD");
    	String CUST_NM = requestBody.get("CUST_NM");
    	String CUST_GOOID = requestBody.get("CUST_GOOID");
    	
    	// 텍스트 콘텐츠
    	String stContent = requestBody.get("FORM_DETAIL");
	 
    	stContent = stContent.replaceAll("&nbsp;", "&#160;");
    			 
	 	// Html Making Start
    	StringBuilder content = new StringBuilder("<html>");
        content.append("<head><style>")
        .append("body { font-family: 'NotoSansKR'; }")
        .append("</style></head>")
		.append("<body>")
		.append("<p>제목</p>")
		.append(stContent)
		.append("</body></html>");
		 
		String filePath = "C:\\Users\\User\\Desktop\\fileTest\\"+CUST_CD +CUST_NM+".pdf";
		
		requestBody.put("filePath", filePath);
		
		try (FileOutputStream os = new FileOutputStream(filePath)) {
			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.useFastMode();
			builder.withHtmlContent(content.toString(), null);
			builder.useFont(new File("C:\\Users\\User\\git\\eSignify\\NotoSansKR-VariableFont_wght.ttf"), "NotoSansKR");
			builder.toStream(os);
			builder.run();
			System.out.println("PDF 생성 완료: ");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return filePath;

    }
    
//    public String createRawEmail(String to, String subject, String bodyText) {
//    	 // SMTP 설정
//        Properties props = new Properties();
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.smtp.host", "smtp.gmail.com");
//        props.put("mail.smtp.port", "587");
//
//        // 인증 추가
//        Session session = Session.getInstance(props, new Authenticator() {
//            @Override
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication("your_email@gmail.com", "your_password");
//            }
//        });
//
//        try {
//            // MIME 메시지 생성
//            MimeMessage mimeMessage = new MimeMessage(session);
//            mimeMessage.setFrom(new InternetAddress("your_email@gmail.com"));
//            mimeMessage.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress("recipient@example.com"));
//            mimeMessage.setSubject("Test Email");
//            mimeMessage.setText("This is a test email body.");
//
//            System.out.println("MimeMessage created successfully.");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


}
