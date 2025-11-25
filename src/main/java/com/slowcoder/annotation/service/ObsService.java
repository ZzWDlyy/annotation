package com.slowcoder.annotation.service;

import com.obs.services.ObsClient;
import com.obs.services.model.PutObjectResult;
import com.obs.services.model.TemporarySignatureRequest;
import com.obs.services.model.TemporarySignatureResponse;
import com.obs.services.model.HttpMethodEnum;
import com.slowcoder.annotation.config.ObsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ObsService {

    @Autowired
    private ObsClient obsClient;

    @Autowired
    private ObsConfig obsConfig;

    /**
     * 生成临时上传凭证（前端直传用）
     * @param fileName 文件名
     * @param expireSeconds 有效期（秒）
     * @return 临时签名 URL
     */
    public Map<String, String> generateUploadUrl(String fileName, long expireSeconds) {
        // 生成唯一文件名（防止重名覆盖）
        String objectKey = generateObjectKey(fileName);

        // 创建临时签名请求
        TemporarySignatureRequest request = new TemporarySignatureRequest(
                HttpMethodEnum.PUT,  // 上传用 PUT
                expireSeconds
        );
        request.setBucketName(obsConfig.getBucketName());
        request.setObjectKey(objectKey);

        // 生成签名 URL
        TemporarySignatureResponse response = obsClient.createTemporarySignature(request);

        Map<String, String> result = new HashMap<>();
        result.put("uploadUrl", response.getSignedUrl());  // 前端用这个 URL 上传
        result.put("fileUrl", obsConfig.getBaseUrl() + "/" + objectKey);  // 上传后的访问地址
        result.put("objectKey", objectKey);

        return result;
    }

    /**
     * 后端直接上传文件（方案2）
     * @param file 文件
     * @param projectId 项目ID
     * @return 文件访问 URL
     */
    public String uploadFile(MultipartFile file, Long projectId) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String objectKey = "project-" + projectId + "/" + generateObjectKey(originalFilename);

        // 上传到 OBS
        PutObjectResult result = obsClient.putObject(
                obsConfig.getBucketName(),
                objectKey,
                file.getInputStream()
        );

        if (result.getStatusCode() == 200) {
            return obsConfig.getBaseUrl() + "/" + objectKey;
        } else {
            throw new RuntimeException("上传失败：" + result.getStatusCode());
        }
    }

    /**
     * 批量生成上传凭证（一次性上传多个文件）
     * @param fileNames 文件名列表
     * @param expireSeconds 有效期
     * @return 每个文件的上传信息
     */
    public Map<String, Map<String, String>> generateBatchUploadUrls(
            java.util.List<String> fileNames,
            long expireSeconds
    ) {
        Map<String, Map<String, String>> result = new HashMap<>();
        for (String fileName : fileNames) {
            result.put(fileName, generateUploadUrl(fileName, expireSeconds));
        }
        return result;
    }

    /**
     * 生成唯一的对象键（防止文件名冲突）
     */
    private String generateObjectKey(String originalFilename) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 格式：2025/01/15/uuid.jpg
        String datePrefix = java.time.LocalDate.now().toString().replace("-", "/");
        return datePrefix + "/" + uuid + extension;
    }
}
