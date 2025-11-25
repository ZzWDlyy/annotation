package com.slowcoder.annotation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterReq {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 6,max = 20,message = "用户名长度必须在6-20字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度至少 6 位")
    private String password;
    private String realName;
    private Integer gender;      // 对应数据库 tinyint
    private String ageRange;     // e.g. "20-25"
    private String mbti;         // e.g. "INTJ"
    private String education;    // e.g. "本科"
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "验证码不能为空")
    private String captcha;  // 用户输入的验证码
}
