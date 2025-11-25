package com.slowcoder.annotation.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import com.slowcoder.annotation.entity.User;
import com.slowcoder.annotation.mapper.UserMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 自定义权限验证接口扩展
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private final UserMapper userMapper;

    public StpInterfaceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 本项目暂时只用角色控制，权限码留空即可
        return new ArrayList<>();
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限验证的核心)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 1. loginId 就是 userId (我们在登录时 StpUtil.login(user.getId()) 存进去的)
        Long userId = Long.valueOf(loginId.toString());

        // 2. 查数据库获取用户
        User user = userMapper.selectById(userId);

        // 3. 返回角色列表
        if (user != null && user.getRole() != null) {
            // 假设数据库里存的是 "admin" 或 "annotator"
            return Collections.singletonList(user.getRole());
        }
        return new ArrayList<>();
    }
}
