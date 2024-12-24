package com.eSignify.common.google.entity;

import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMailEntity {
    private String custMap; // 고객 정보
    private String pdfMap;  // PDF 관련 데이터
    private String mailMap; // 메일 관련 데이터
}
