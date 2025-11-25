package com.slowcoder.annotation.dto;

import com.slowcoder.annotation.entity.AnnotationRecord;
import lombok.Data;

@Data
public class AnnotationRecordVO extends AnnotationRecord {
    private String assetUrl;   // 图片链接
    private String username;   // 标注员名字

}