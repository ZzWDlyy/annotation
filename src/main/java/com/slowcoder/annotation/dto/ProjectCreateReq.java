package com.slowcoder.annotation.dto;

import com.slowcoder.annotation.entity.ProjectTask;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProjectCreateReq {
    @NotBlank(message = "任务名称不能为空")
    private String taskName;
    private String description;
    @NotBlank(message = "媒体类型不能为空")
    private String mediaType;
    @NotNull(message = "每张图的目标标注数不能为空")
    @Min(value = 1, message = "至少需要1人标注")
    private Integer targetPerAsset;
    @NotEmpty(message = "题目配置不能为空") // 集合判空用 NotEmpty
    List<ProjectTask.TaskItem> config;
}
