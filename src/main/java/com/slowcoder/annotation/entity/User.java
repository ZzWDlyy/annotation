package com.slowcoder.annotation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "sys_user", autoResultMap = true)
public class User {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String email;  // 新增邮箱字段
    private String realName;
    private String role;
    private Integer gender;
    private String ageRange;
    private String mbti;
    private String education;
    private Integer score;
    private LocalDateTime createTime;
}
