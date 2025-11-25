package com.slowcoder.annotation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_assignment")
public class TaskAssignment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;    // 所属项目ID
    private Long assetId;   // 图片ID
    private Long userId;    // 指派给谁

    // 核心字段：区分是管理员派的(PUSH)还是自己抢的(PULL)
    private String assignType;

    // 0-待标, 1-已标, 2-过期/放弃
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime expireTime; // 比如抢单后30分钟内必须提交

    private LocalDateTime finishTime;
}
