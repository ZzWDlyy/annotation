package com.slowcoder.annotation.dto;

import lombok.Data;

@Data
public class StatUserDTO {
    private Long userId;
    private String username;
    private Long totalSubmitted;   // 总提交数
    private Long rejectedCount;    // 被驳回数
    private Double rejectedRate;   // 驳回率
    private Double avgTimeCost;    // 平均耗时(秒)
}
