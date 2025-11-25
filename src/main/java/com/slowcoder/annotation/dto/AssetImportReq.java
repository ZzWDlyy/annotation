package com.slowcoder.annotation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AssetImportReq {
    @NotNull(message = "项目ID不能为空")
    private Long projectId;
    @NotEmpty(message = "url链接不能为空")
    List<String> urls;
}
