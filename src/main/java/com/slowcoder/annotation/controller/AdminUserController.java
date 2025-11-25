package com.slowcoder.annotation.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.slowcoder.annotation.common.Result;
import com.slowcoder.annotation.dto.AddUserReq;
import com.slowcoder.annotation.entity.User;
import com.slowcoder.annotation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Autowired
    private UserService userService;

    /**
     * 查询用户列表（分页）
     */
    @GetMapping("/list")
    @SaCheckRole("admin")
    public Result<Page<User>> listUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword  // 搜索关键词
    ) {
        Page<User> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword);
        }

        Page<User> result = userService.page(page, wrapper);

        // 隐藏密码
        result.getRecords().forEach(user -> user.setPassword(null));

        return Result.success(result);
    }

    /**
     * 管理员添加用户
     */
    @PostMapping("/add")
    @SaCheckRole("admin")
    public Result<String> addUser(@Valid @RequestBody  AddUserReq req) {
        userService.addUserByAdmin(req);
        return Result.success("用户创建成功");
    }

    /**
     * 删除用户（逻辑删除）
     */
    @DeleteMapping("/{userId}")
    @SaCheckRole("admin")
    public Result<String> deleteUser(@PathVariable Long userId) {
        // 不能删除管理员自己
        if (userId.equals(StpUtil.getLoginIdAsLong())) {
            return Result.failed("不能删除自己");
        }

        boolean success = userService.removeById(userId);
        return success ? Result.success("删除成功") : Result.failed("删除失败");
    }



    /**
     * 修改用户角色
     */
    @PutMapping("/{userId}/role")
    @SaCheckRole("admin")
    public Result<String> changeRole(
            @PathVariable Long userId,
            @RequestParam String role  // admin 或 annotator
    ) {
        if (!role.equals("admin") && !role.equals("annotator")) {
            return Result.failed("角色参数错误");
        }

        User user = new User();
        user.setId(userId);
        user.setRole(role);

        boolean success = userService.updateById(user);
        return success ? Result.success("角色修改成功") : Result.failed("操作失败");
    }
}
