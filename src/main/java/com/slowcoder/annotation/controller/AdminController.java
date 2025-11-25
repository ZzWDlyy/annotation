package com.slowcoder.annotation.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.slowcoder.annotation.common.Result;
import com.slowcoder.annotation.dto.*;
import com.slowcoder.annotation.entity.AnnotationRecord;
import com.slowcoder.annotation.mapper.AnnotationRecordMapper;
import com.slowcoder.annotation.service.TaskService;
import com.slowcoder.annotation.service.impl.TaskServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final TaskService taskService;

    public AdminController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/review/list")
    @SaCheckRole("admin")
    public Result<Page<AnnotationRecordVO>> listReviews(@RequestBody ReviewQueryReq req) {
        // 一行代码调用 Service
        Page<AnnotationRecordVO> page = taskService.queryReviewRecords(req);
        return Result.success(page);
    }

    @PostMapping("/review/reject")
    @SaCheckRole("admin")
    public Result<String> reject(@RequestBody RejectReq req) {
        // RejectReq 包含: Long recordId, String reason
        taskService.rejectAnnotation(req.getRecordId(), req.getReason());
        return Result.success("已驳回，该任务已释放回公海池");
    }

    @GetMapping("/stat/project/{taskId}")
    @SaCheckRole("admin")
    public Result<StatProjectDTO> getProjectStats(@PathVariable Long taskId) {
        return Result.success(taskService.getProjectStats(taskId));
    }

    @GetMapping("/stat/users/{taskId}")
    @SaCheckRole("admin")
    public Result<List<StatUserDTO>> getUserStats(@PathVariable Long taskId) {
        return Result.success(taskService.getUserStats(taskId));
    }

    @GetMapping("/export/excel/{taskId}")
    @SaCheckRole("admin")
    public void exportExcel(@PathVariable Long taskId, HttpServletResponse response) throws IOException {
        List<ExportRecordDTO> data = taskService.exportDataToExcel(taskId,response);

        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("标注结果_" + taskId, "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), ExportRecordDTO.class)
                .sheet("标注结果")
                .doWrite(data);
    }

}
