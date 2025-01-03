package com.eSignify.common;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eSignify.google.service.GoogleDriveUploader;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@Service
public class PdfService {
	
	@Autowired
	GoogleDriveUploader googleDriveUploader;
	
    // OpenHTMLToPDF로 PDF 변환 후 googlrDrive 업로드 
    public String createPDF(HashMap<String, Object> custMap,HashMap<String, Object> 
    										pdfMap,HashMap<String, Object> mailMap,String googleAccessToken) {

    	String custCd 	  = (String) custMap.get("cust_cd");
    	String custNm 	  = (String) custMap.get("cust_nm");
    	String custGooId = (String) custMap.get("cust_gooid");
    	
    	// 텍스트 콘텐츠
    	String pdfFormDetail = (String) pdfMap.get("form_detail");
	 
    	pdfFormDetail = pdfFormDetail.replaceAll("&nbsp;", "&#160;");
    			 
	 	// Html Making Start
    	StringBuilder content = new StringBuilder("<html>");
        content.append("<head><style>")
        .append("body { "
        		+ "font-family: 'MaruBuri'; "
        		+ "color: #000;"
        		+ "font-size: 14px;"
        		+ "text-align: center;"
        		+ "}")
        .append("</style></head>")
		.append("<body>")
		.append("<h1>")
		.append("제목11")
		.append("</h1>")
		.append(pdfFormDetail)
		.append("</body></html>");
		 
        String fileName = custCd +custNm+".pdf";
        
		try {
			
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			PdfRendererBuilder builder = new PdfRendererBuilder();

			builder.useFastMode();
			builder.withHtmlContent(content.toString(), null);
			builder.useFont(new File("C:\\Users\\User\\git\\eSignify\\MaruBuri-Regular.ttf"), "MaruBuri");
			builder.toStream(outputStream);
			builder.run();
			System.out.println("PDF 생성 완료: ");
			
			ByteArrayOutputStream signatureOs = addSignatureToPdf(outputStream,"123");
			
			String fileId = googleDriveUploader.uploadPdfToDrive(signatureOs, custCd +custNm,googleAccessToken);
			
			return fileId;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

    }
    
    // 원하는 좌표에 서명 추가
    public ByteArrayOutputStream addSignatureToPdf(ByteArrayOutputStream pdfStream, String signatureText) {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfStream.toByteArray()));
             ByteArrayOutputStream signedPdfStream = new ByteArrayOutputStream()) {

            // 첫 번째 페이지에 서명 추가
            PDPage page = document.getPage(0);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(100, 100); // 서명 위치 (x, y)
                contentStream.showText(signatureText); // 서명 텍스트
                contentStream.endText();
            }

            // 서명된 PDF를 스트림에 저장
            document.save(signedPdfStream);
            return signedPdfStream;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error adding signature to PDF", e);
        }
    }
    
    // PDF를 Base64 인코딩하여 JSON 응답으로 반환
    public List<String> convertPdfToBase64Images(File pdfFile) throws IOException {
        List<String> base64Images = new ArrayList<>();
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    ImageIO.write(image, "PNG", baos);
                    String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
                    base64Images.add(base64Image);
                }
            }
        }
        
        return base64Images;

    }

    
    // PDF 다운로드
    public static java.io.File downloadPdfFromLink(String fileId) throws IOException {
        String downloadUrl = "https://drive.google.com/uc?id=" + fileId;
        java.io.File pdfFile = new java.io.File("downloaded.pdf");

        HttpURLConnection connection = (HttpURLConnection) new URL(downloadUrl).openConnection();
        connection.setRequestMethod("GET");
        try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
             FileOutputStream fileOutputStream = new FileOutputStream(pdfFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        }
        
        // PDF 유효성 검사
        byte[] fileContent = Files.readAllBytes(pdfFile.toPath());
        if (!isValidPdf(fileContent)) {
            throw new IOException("Invalid PDF file");
        }

        
        return pdfFile;
    }
    
    public String extractFileId(String sharedLink) {
        String[] parts = sharedLink.split("/");
        return parts[parts.length - 2]; // 파일 ID 추출
    }
    
    private static boolean isValidPdf(byte[] fileContent) {
        String header = new String(fileContent, 0, Math.min(fileContent.length, 5));
        return header.startsWith("%PDF-");
    }
    
}
