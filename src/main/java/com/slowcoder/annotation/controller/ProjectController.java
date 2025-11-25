package com.slowcoder.annotation.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.slowcoder.annotation.common.Result;
import com.slowcoder.annotation.dto.AssetImportReq;
import com.slowcoder.annotation.dto.ProjectCreateReq;
import com.slowcoder.annotation.service.ObsService;
import com.slowcoder.annotation.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/project")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ObsService obsService;

    /**
     * 创建项目
     */
    @PostMapping("/create")
    @SaCheckRole("admin")
    public Result<Long> createProject(@RequestBody @Valid ProjectCreateReq req) {
        Long projectId = projectService.createProject(req);
        return Result.success(projectId);
    }

    /**
     * 【方案1】获取上传凭证（前端直传）
     */
    @PostMapping("/upload/token")
    @SaCheckRole("admin")
    public Result<Map<String, String>> getUploadToken(@RequestParam String fileName) {
        Map<String, String> uploadInfo = obsService.generateUploadUrl(fileName, 3600); // 1小时有效
        return Result.success(uploadInfo);
    }

    /**
     * 【方案1】批量获取上传凭证
     */
    @PostMapping("/upload/batch-token")
    @SaCheckRole("admin")
    public Result<Map<String, Map<String, String>>> getBatchUploadTokens(
            @RequestBody List<String> fileNames
    ) {
        Map<String, Map<String, String>> uploadInfos = obsService.generateBatchUploadUrls(fileNames, 3600);
        return Result.success(uploadInfos);
    }

    /**
     * 【方案2】后端接收文件并上传到OBS（单个文件）
     */
    @PostMapping("/upload/file")
    @SaCheckRole("admin")
    public Result<String> uploadFile(
            @RequestParam Long projectId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        String fileUrl = obsService.uploadFile(file, projectId);
        return Result.success(fileUrl);
    }

    /**
     * 【方案2】后端批量上传
     */
    @PostMapping("/upload/batch-file")
    @SaCheckRole("admin")
    public Result<List<String>> uploadBatchFiles(
            @RequestParam Long projectId,
            @RequestParam("files") MultipartFile[] files
    ) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = obsService.uploadFile(file, projectId);
            urls.add(url);
        }
        return Result.success(urls);
    }

    /**
     * 导入资源（你原有的接口，无需修改）
     */
    @PostMapping("/import")
    @SaCheckRole("admin")
    public Result<String> importAssets(@RequestBody @Valid AssetImportReq req) {
        boolean success = projectService.importAssets(req);
        return success ? Result.success("导入成功") : Result.failed("导入失败");
    }
}
