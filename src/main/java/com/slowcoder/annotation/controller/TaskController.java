package com.slowcoder.annotation.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.slowcoder.annotation.common.Result;
import com.slowcoder.annotation.dto.StatUserDTO;
import com.slowcoder.annotation.dto.SubmitReq;
import com.slowcoder.annotation.dto.TaskGrabReq;
import com.slowcoder.annotation.entity.TaskAssignment;
import com.slowcoder.annotation.mapper.TaskAssignmentMapper;
import com.slowcoder.annotation.service.TaskService;
import com.slowcoder.annotation.service.impl.TaskServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/task/")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/my-list")
    public List<TaskAssignment> getMyList() {
        return taskService.getMyList();
    }

    @PostMapping("/grab")
    @SaCheckRole("annotator")
    public Result<List<TaskAssignment>> grabTask(@RequestBody TaskGrabReq req) {
        // 1. 安全修正：强制设置为当前登录人的 ID
        long currentUserId = StpUtil.getLoginIdAsLong();
        req.setUserId(currentUserId);

        // 2. 调用服务
        List<TaskAssignment> list = taskService.grabTask(req);
        return Result.success(list);
    }

    @PostMapping("/submit")
    @SaCheckRole("annotator")
    public Result<String> submitTask(@RequestBody SubmitReq req) {
        // 获取当前登录用户ID (假设你用了 Sa-Token)
        long userId = StpUtil.getLoginIdAsLong();
        taskService.submitTask(userId, req);
        return Result.success("提交成功");
    }

    @GetMapping("/stat/users/{taskId}")
    @SaCheckRole("admin")
    public Result<List<StatUserDTO>> getUserStats(@PathVariable Long taskId) {
        return Result.success(taskService.getUserStats(taskId));
    }
}
