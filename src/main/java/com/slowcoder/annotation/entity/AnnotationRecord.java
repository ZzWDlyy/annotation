package com.slowcoder.annotation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "annotation_record",autoResultMap = true)
public class AnnotationRecord {
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long assetId;
    private Long userId;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String,Object> resultData;
    private Integer duration;
    private Integer reviewStatus;
    private LocalDateTime createTime;
}
