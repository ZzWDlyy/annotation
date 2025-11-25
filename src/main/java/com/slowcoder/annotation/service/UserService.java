package com.slowcoder.annotation.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.slowcoder.annotation.dto.AddUserReq;
import com.slowcoder.annotation.dto.LoginReq;
import com.slowcoder.annotation.dto.UserRegisterReq;
import com.slowcoder.annotation.entity.User;

public interface UserService extends IService<User> {
    public void register(UserRegisterReq req);
    public SaTokenInfo login(LoginReq req);
    public User getCurrentUserInfo();
    void addUserByAdmin(AddUserReq req);

}
