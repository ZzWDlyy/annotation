package com.slowcoder.annotation.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.slowcoder.annotation.common.Result;
import com.slowcoder.annotation.dto.LoginReq;
import com.slowcoder.annotation.dto.UserRegisterReq;
import com.slowcoder.annotation.entity.User;
import com.slowcoder.annotation.service.CaptchaService;
import com.slowcoder.annotation.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final CaptchaService captchaService;
    public AuthController(UserService userService, CaptchaService captchaService) {
        this.userService = userService;
        this.captchaService = captchaService;
    }
    @PostMapping("/captcha/email")
    public Result<String> sendEmailCaptcha(@RequestParam String email) {
        // 简单的邮箱格式校验
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return Result.failed("邮箱格式不正确");
        }

        captchaService.sendEmailCaptcha(email);
        return Result.success("验证码已发送，请查收邮件");
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody @Valid UserRegisterReq req) {
        userService.register(req);
        return Result.success("注册成功");
    }
    @PostMapping("/login")
    public Result<SaTokenInfo> login(@Valid @RequestBody LoginReq req) {
        SaTokenInfo tokenInfo = userService.login(req);
        return Result.success(tokenInfo);
    }

    @GetMapping("/info")
    public Result<User> getUserInfo() {
        return Result.success(userService.getCurrentUserInfo());
    }
}
