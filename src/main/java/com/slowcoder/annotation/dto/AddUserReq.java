package com.slowcoder.annotation.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class AddUserReq {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "邮箱不能为空")
    private String email;

    private String role = "annotator";  // 默认为标注员

    // 可选字段
    private Integer gender;
    private String ageRange;
    private String mbti;
    private String education;
}
