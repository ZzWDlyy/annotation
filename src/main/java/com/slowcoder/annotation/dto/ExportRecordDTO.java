package com.slowcoder.annotation.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ExportRecordDTO {
    private String assetUrl;              // 图片URL
    private Integer labelCount;           // 标注次数
    private String annotatorIds;          // 标注者ID列表
    private Map<String, Object> aggregatedData; // 聚合后的标注数据（动态字段）
}
