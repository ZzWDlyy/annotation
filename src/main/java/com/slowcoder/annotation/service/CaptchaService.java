package com.slowcoder.annotation.service;

public interface CaptchaService {
    public void sendEmailCaptcha(String email);
    public boolean verifyCaptcha(String email, String code);
}
