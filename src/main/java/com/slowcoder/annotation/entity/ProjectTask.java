package com.slowcoder.annotation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// 1. 开启 autoResultMap，否则读取时字段会是 null
@Data
@TableName(value = "project_task", autoResultMap = true)
public class ProjectTask implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskName;
    private String description;

    // 2. 重点：使用 JacksonTypeHandler 自动转换
    // 这里我们将 JSON 映射为一个 List<TaskItem> 对象
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<TaskItem> configTemplate;

    private String mediaType;
    private Integer targetPerAsset;
    private Integer status;
    private LocalDateTime createTime;

    public List<Map<String, Object>> getConfig() {
        return null;
    }

    /**
     * 定义 JSON 内部结构的 POJO
     * 对应数据库存的: [{"label":"构图","type":"slider"}, ...]
     */
    @Data
    public static class TaskItem implements Serializable {
        private String key;      // 字段名，如 "composition"
        private String label;    // 显示名，如 "构图评分"
        private String type;     // 组件类型，如 "slider", "radio", "bbox"
        private Boolean required;// 是否必填

        // 扩展字段，有的题目有min/max，有的有options，用变量存
        private Integer min;
        private Integer max;
        private List<Object> options;
    }
}
