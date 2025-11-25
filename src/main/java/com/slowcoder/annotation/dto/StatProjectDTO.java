package com.slowcoder.annotation.dto;

import lombok.Data;

@Data
public class StatProjectDTO {
    private Long taskId;
    private String taskName;
    private Integer totalAssets;      // 总图片数
    private Integer completedAssets;  // 已完成图片数
    private Double progress;          // 完成百分比
    private Long totalAnnotations;    // 已提交标注数
    private Long targetAnnotations;   // 目标标注数 (总数 * targetPerAsset)
}
