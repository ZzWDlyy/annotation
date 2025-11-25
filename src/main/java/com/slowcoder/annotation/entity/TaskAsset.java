package com.slowcoder.annotation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "task_asset", autoResultMap = true)
public class TaskAsset {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String assetUrl;
    private Integer labeledCount;
    private Integer status;
    private LocalDateTime createTime;
}
