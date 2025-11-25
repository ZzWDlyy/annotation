package com.slowcoder.annotation.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.slowcoder.annotation.common.Result;
import com.slowcoder.annotation.dto.AssignTaskReq;
import com.slowcoder.annotation.service.impl.TaskServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assign")
public class AssignController {
    private final TaskServiceImpl taskService;

    public AssignController(TaskServiceImpl taskService) {
        this.taskService = taskService;
    }


    @PostMapping("/push")
    @SaCheckRole("admin") // 如果你集成了SaToken，记得加上这个
    public Result<String> pushTask(@RequestBody AssignTaskReq req) {
        boolean success = taskService.assignTasks(req);
        if (success) {
            return Result.success("派单成功");
        } else {
            return Result.failed("派单失败，可能没有足够的剩余图片，或该用户已拥有这些任务");
        }
    }

}
