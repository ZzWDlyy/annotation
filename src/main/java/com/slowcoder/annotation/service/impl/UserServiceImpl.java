package com.slowcoder.annotation.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slowcoder.annotation.dto.AddUserReq;
import com.slowcoder.annotation.dto.LoginReq;
import com.slowcoder.annotation.dto.UserRegisterReq;
import com.slowcoder.annotation.entity.User;
import com.slowcoder.annotation.mapper.UserMapper;
import com.slowcoder.annotation.service.CaptchaService;
import com.slowcoder.annotation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private CaptchaService captchaService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterReq req) {
        // 0. 【新增】校验验证码
        if (!captchaService.verifyCaptcha(req.getEmail(), req.getCaptcha())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 1. 校验用户名是否存在
        long count = this.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));
        if (count > 0) {
            throw new RuntimeException("用户名已存在，请更换");
        }

        // 1.5 【新增】校验邮箱是否已注册
        long emailCount = this.count(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, req.getEmail()));
        if (emailCount > 0) {
            throw new RuntimeException("该邮箱已被注册");
        }

        // 2. 构建 User 对象
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(BCrypt.hashpw(req.getPassword()));
        user.setRealName(req.getRealName());
        user.setEmail(req.getEmail());  // 【新增】保存邮箱
        user.setGender(req.getGender());
        user.setAgeRange(req.getAgeRange());
        user.setMbti(req.getMbti());
        user.setEducation(req.getEducation());

        // 3. 设置默认值
        user.setRole("annotator");
        user.setScore(100);
        user.setCreateTime(LocalDateTime.now());

        // 4. 写入数据库
        this.save(user);
    }

    @Override
    public SaTokenInfo login(LoginReq req) {
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!BCrypt.checkpw(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        StpUtil.login(user.getId());
        return StpUtil.getTokenInfo();
    }

    @Override
    public User getCurrentUserInfo() {
        long userId = StpUtil.getLoginIdAsLong();
        User user = this.getById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUserByAdmin(AddUserReq req) {
        // 1. 校验用户名是否存在
        long count = this.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 校验邮箱是否已注册
        long emailCount = this.count(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, req.getEmail()));
        if (emailCount > 0) {
            throw new RuntimeException("该邮箱已被注册");
        }

        // 3. 创建用户
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(BCrypt.hashpw(req.getPassword()));  // 加密密码
        user.setRealName(req.getRealName());
        user.setEmail(req.getEmail());
        user.setRole(req.getRole());
        user.setGender(req.getGender());
        user.setAgeRange(req.getAgeRange());
        user.setMbti(req.getMbti());
        user.setEducation(req.getEducation());
        user.setScore(100);
        user.setCreateTime(LocalDateTime.now());

        this.save(user);
    }

}
