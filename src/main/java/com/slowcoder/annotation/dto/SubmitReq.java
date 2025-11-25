package com.slowcoder.annotation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

@Data
public class SubmitReq {
    @NotBlank(message = "任务ID不能为空")
    private Long taskId;
    @NotBlank(message = "标注ID不能为空")
    private Long assetId;
    @NotEmpty(message = "标注结果不能为空")
    private Map<String,Object> resultData;
    private Integer timeCost;
}
