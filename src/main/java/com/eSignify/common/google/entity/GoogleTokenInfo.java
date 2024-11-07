package com.eSignify.common.google.entity;

public class GoogleTokenInfo {
    private String aud;
    private String sub;
    private String email;

    // getter와 setter 메서드
    public String getAud() {
        return aud;
    }

    public void setAud(String aud) {
        this.aud = aud;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
