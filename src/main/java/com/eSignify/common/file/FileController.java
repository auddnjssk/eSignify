package com.eSignify.common.file;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.eSignify.common.CommonUtil;
import com.eSignify.common.MessageHttpResponse;
import com.eSignify.common.ObjectUtil;
import com.eSignify.common.ResponseDTO;
import com.eSignify.common.utils.Constants;
import com.eSignify.common.utils.JwtTokenUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class FileController {
	
	
	@Autowired
	CommonUtil commonUtil;
	
	@Autowired 
	private JwtTokenUtil jwtTokenUtil; 
	
	@Autowired
	MessageHttpResponse messageHttpResponse;
	
	@Value("${imageFileDir}")
	private String imageFileDir;
	
	
	
	@GetMapping("/image")
	public ResponseEntity<?> getImage(@RequestHeader("Authorization") String authorizationHeader ,@RequestParam("formId") String formId,
			HttpServletRequest request,HttpServletResponse response ) {
		
		try {
			
			List<String> fileUrlList = new ArrayList<>();

			
			// 파일 저장 처리
			String imageFileDir = "C:\\Temp"; // 권한 보장된 경로로 변경
			
			// 파일 저장
			
			String token = authorizationHeader.replace("Bearer ", "");
			
			ObjectMapper ob = new ObjectMapper();
			
			try {
				JsonNode jn = ob.readTree(token);
				token = jn.get("value").asText();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			
			String userId = jwtTokenUtil.getUserIdFromToken(token);
			

			
			for(int i=1 ;5 > i ; i++) {
				String fileName = formId + "_" + userId + "_"+i+".png";
				
				URL fileUrl = findFileInDirectory(fileName);
				
				if(ObjectUtil.isEmpty(fileUrl)) break;
				
				fileUrlList.add(i-1, fileName);
				
			}
			
			
	        
			ResponseDTO responseDTO = new ResponseDTO.Builder()
					.setMessage("파일 URL 다운로드")
					.setResult(fileUrlList)
					.setStatusCode(200)
					.build();
			
			return messageHttpResponse.success(responseDTO);
		} catch (Exception e) {
			System.out.println("e"+e.toString());
			e.printStackTrace();
			return ResponseEntity.status(500).body("파일 업로드 실패!");
		}
	}
	@PostMapping("/imageUpload")
	public ResponseEntity<?> imageDownload(@RequestHeader("Authorization") String authorizationHeader ,@RequestParam("image") MultipartFile file,
			HttpServletRequest request,HttpServletResponse response ) {
		
		try {
            // 파일 저장 처리
            String fileNamefsaf = file.getOriginalFilename();
            String uploadDir = "C:\\Temp"; // 권한 보장된 경로로 변경

            // 파일 저장
            String fileName = file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.]", "_");
    		String token = authorizationHeader.replace("Bearer ", "");

    		ObjectMapper ob = new ObjectMapper();
    		
    		try {
    			JsonNode jn = ob.readTree(token);
    			token = jn.get("value").asText();
    		} catch (JsonMappingException e) {
    			e.printStackTrace();
    		} catch (JsonProcessingException e) {
    			e.printStackTrace();
    		}
    		
    		String userId = jwtTokenUtil.getUserIdFromToken(token);
    		
            File directory = new File(uploadDir);
            
            if (!directory.exists()) {
                directory.mkdirs(); // 디렉토리가 없으면 생성
                System.out.println("디렉토리 생성됨: " + uploadDir);
            }
            
            fileName = fileName.replace("userId", userId);
            
            String filePath = uploadDir + File.separator + fileName;
            
            System.out.println("저장 경로: " + filePath);
            System.out.println("업로드된 파일 크기: " + file.getSize());
            
            file.transferTo(new File(filePath));

            // 저장 확인
            File savedFile = new File(filePath);
            
            if (savedFile.exists()) {
                System.out.println("파일 저장 성공: " + filePath);
            } else {
                System.out.println("파일이 저장되지 않았습니다.");
            }
            return ResponseEntity.ok("파일 업로드 성공! 파일명: " + fileName);
        } catch (Exception e) {
        	System.out.println("e"+e.toString());
            e.printStackTrace();
            return ResponseEntity.status(500).body("파일 업로드 실패!");
        }
	}
	
	
	public static URL findFileInDirectory(String fileName) {
        try {
        	
        	String directoryPath = "C:\\Temp"; // 권한 보장된 경로로 변경
        	
            // 디렉토리를 Path 객체로 변환
            Path directory = Paths.get(directoryPath);

            // 디렉토리 내 모든 파일 검색
            return Files.walk(directory)
                    .filter(Files::isRegularFile) // 정규 파일만 선택
                    .filter(path -> path.getFileName().toString().equals(fileName)) // 파일 이름이 일치하는지 확인
                    .map(Path::toUri) // URI로 변환
                    .map(uri -> {
                        try {
                            return uri.toURL(); // URL로 변환
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e); // 변환 에러 처리
                        }
                    })
                    .findFirst() // 첫 번째 결과 반환
                    .orElse(null); // 결과가 없으면 null 반환
        } catch (IOException e) {
            e.printStackTrace();
            return null; // 에러가 발생하면 null 반환
        }
    }

	
	
}
