package com.slowcoder.annotation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaskGrabReq {
    @NotBlank(message = "项目id不能为空")
    private Long taskId;
    @NotBlank(message = "用户id不能为空")
    private Long userId;
    @NotBlank(message = "分配数量不能为空")
    private Integer count;
}
