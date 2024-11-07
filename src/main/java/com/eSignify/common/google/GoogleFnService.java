package com.eSignify.common.google;

import java.util.Base64;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
/**
* ���� ��� ����
* @author �����
* @since 2024. 11. 6.
* @version
*/

@Service
public class GoogleFnService {
	
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * ���� �̸��� ���� ���
     * @param String accessToken , to , subject , bodyText
     * @return ResponseEntity
     */
    public String sendEmail(String accessToken, String to, String subject, String bodyText) {
        String url = "https://www.googleapis.com/gmail/v1/users/me/messages/send";
        
        to = "nxnx23@naver.com";
        
        // �̸��� �޽��� �ۼ�
        String rawMessage = createRawEmail(to, subject, bodyText);
        
        // HTTP ��û ��� ����
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        // ��û �ٵ� ����
        String requestBody = "{ \"raw\": \"" + rawMessage + "\" }";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        // API ȣ��
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        
        return response.getBody();
    }

    private String createRawEmail(String to, String subject, String bodyText) {
        String raw = "To: " + to + "\r\n" +
                     "Subject: " + subject + "\r\n" +
                     "Content-Type: text/plain; charset=utf-8\r\n\r\n" +
                     bodyText;

        return Base64.getUrlEncoder().encodeToString(raw.getBytes());
    }

}
