package com.slowcoder.annotation.dto;

import lombok.Data;

@Data
public class ReviewQueryReq {
    // 分页参数
    private Integer page = 1;
    private Integer size = 10;

    // 筛选条件 (全部可选)
    private Long taskId;    // 只看某个项目的
    private Long assetId;   // 只看某张图的
    private Long userId;    // 只看某人的
    private Integer reviewStatus;  // 只看某种状态的 (0-未标，1-有效, 2-驳回)
}
