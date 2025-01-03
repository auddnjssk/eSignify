package com.eSignify.google.service;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

import org.springframework.stereotype.Service;

@Service
public class GoogleDriveUploader {

    private static final String APPLICATION_NAME = "PDF Uploader";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    // 
    public static String uploadPdfToDrive(ByteArrayOutputStream pdfOutputStream, String fileName, String accessToken) throws Exception {
        
    	// Access Token을 사용하여 HttpRequestInitializer 생성
        HttpRequestInitializer requestInitializer = request -> {
            request.setInterceptor(request1 -> request1.getHeaders().setAuthorization("Bearer " + accessToken));
        };
        
        String folderName = "Contract";
        

        // Drive 서비스 초기화
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build();

        // 폴더 검색 또는 생성
        String folderId = findOrCreateFolder(service, folderName);
        
        // PDF 업로드
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setParents(Collections.singletonList(folderId)); // 폴더 ID 설정

        ByteArrayContent mediaContent = new ByteArrayContent("application/pdf", pdfOutputStream.toByteArray());
        File file = service.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();
        
        String fileId = file.getId();
        
        // 파일 공유 설정
        Permission permission = new Permission();
        permission.setType("anyone"); // 모든 사용자에게 공유
        permission.setRole("reader"); // 읽기 전용 권한

        service.permissions().create(fileId, permission)
                .setFields("id")
                .execute();

        // 공유 URL 생성
        //String sharedUrl = "https://drive.google.com/file/d/" + fileId + "/view";
        System.out.println("fileId : " + fileId);
        
        return fileId;
        
    }
    
    // 폴더 검색 메서드
    private static String findOrCreateFolder(Drive service, String folderName) throws Exception {
        // 폴더 검색 쿼리
        String query = String.format("mimeType='application/vnd.google-apps.folder' and name='%s' and trashed=false", folderName);
        FileList result = service.files().list()
                .setQ(query)
                .setFields("files(id, name)")
                .execute();

        // 기존 폴더가 있으면 ID 반환
        if (!result.getFiles().isEmpty()) {
            return result.getFiles().get(0).getId();
        }

        // 폴더가 없으면 새로 생성
        File folderMetadata = new File();
        folderMetadata.setName(folderName);
        folderMetadata.setMimeType("application/vnd.google-apps.folder");

        File folder = service.files().create(folderMetadata)
                .setFields("id")
                .execute();

        System.out.println("Created folder with ID: " + folder.getId());
        return folder.getId();
    }
}
