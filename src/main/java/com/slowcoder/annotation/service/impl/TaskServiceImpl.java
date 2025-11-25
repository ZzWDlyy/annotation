package com.slowcoder.annotation.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slowcoder.annotation.dto.*;
import com.slowcoder.annotation.entity.*;
import com.slowcoder.annotation.mapper.*;
import com.slowcoder.annotation.service.TaskService;
import com.slowcoder.annotation.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskAssignmentMapper, TaskAssignment> implements TaskService {
    private final TaskAssetMapper taskAssetMapper;
    private final TaskAssignmentMapper taskAssignmentMapper;
    private final UserService userService;
    private final ProjectTaskMapper projectTaskMapper;
    private final UserMapper userMapper;
    private final AnnotationRecordMapper annotationRecordMapper;

    public TaskServiceImpl(TaskAssetMapper taskAssetMapper, TaskAssignmentMapper taskAssignmentMapper, UserService userService, ProjectTaskMapper projectTaskMapper, UserMapper userMapper, AnnotationRecordMapper annotationRecordMapper) {
        this.taskAssetMapper = taskAssetMapper;
        this.taskAssignmentMapper = taskAssignmentMapper;
        this.userService = userService;
        this.projectTaskMapper = projectTaskMapper;
        this.userMapper = userMapper;
        this.annotationRecordMapper = annotationRecordMapper;
    }

    @Override
    @Transactional(rollbackFor = {java.lang.Exception.class})
    public boolean assignTasks(AssignTaskReq req) {
        User targetUser = userMapper.selectById(req.getUserId());
        if (targetUser == null) {
            throw new IllegalArgumentException("目标用户(ID=" + req.getUserId() + ")不存在，无法派单！");
        }
        if ("admin".equals(targetUser.getRole())) {
            throw new IllegalArgumentException("无法给管理员分配任务");
        }


        List<Object> assignedAssetIds = taskAssignmentMapper.selectObjs(
                new LambdaQueryWrapper<TaskAssignment>()
                        .select(TaskAssignment::getAssetId) // 只查ID字段
                        .eq(TaskAssignment::getUserId, req.getUserId())
                        .eq(TaskAssignment::getTaskId, req.getTaskId())
        );
        LambdaQueryWrapper<TaskAsset> queryWrapper = new LambdaQueryWrapper<TaskAsset>()
                .eq(TaskAsset::getTaskId, req.getTaskId())
                .eq(TaskAsset::getStatus, 0); // 找未完成的图
        if (assignedAssetIds != null && !assignedAssetIds.isEmpty()) {
            queryWrapper.notIn(TaskAsset::getId, assignedAssetIds);
        }
        queryWrapper.last("limit " + req.getCount());

        List<TaskAsset> candidates = taskAssetMapper.selectList(queryWrapper);
        if (candidates.isEmpty()) {
            return false; // 没有符合条件的图了
        }
        List<TaskAssignment> newAssignments = new ArrayList<>();
        for (TaskAsset candidate : candidates) {
            TaskAssignment newAssignment = new TaskAssignment();
            newAssignment.setTaskId(candidate.getTaskId());
            newAssignment.setUserId(req.getUserId());
            newAssignment.setAssetId(candidate.getId());
            newAssignment.setAssignType("PUSH");
            newAssignment.setStatus(0);
            newAssignment.setCreateTime(LocalDateTime.now());

            // 这里可以设置过期时间，比如派单的任务 24小时内有效
            // assignment.setExpireTime(LocalDateTime.now().plusHours(24));
            newAssignments.add(newAssignment);
        }

        return this.saveBatch(newAssignments);
    }

    @Override
    public List<TaskAssignment> getMyList() {
        Long userID = userService.getCurrentUserInfo().getId();
        LambdaQueryWrapper<TaskAssignment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskAssignment::getUserId, userID).eq(TaskAssignment::getStatus, 0);
        return taskAssignmentMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TaskAssignment> grabTask(TaskGrabReq req) {
        // 1. 梯度校验 (50, 100, 150, 200, 250)
        List<Integer> allowedCounts = Arrays.asList(50, 100, 150, 200, 250);
        if (!allowedCounts.contains(req.getCount())) {
            throw new IllegalArgumentException("抢单数量不合法，只能选择: " + allowedCounts);
        }

        // 2. 检查该用户是否还有大量未完成的囤积任务？(防止恶意囤单)
        // 比如：如果你手里还有超过 500 张没做完，就不让你抢了
        long pendingCount = taskAssignmentMapper.selectCount(new LambdaQueryWrapper<TaskAssignment>()
                .eq(TaskAssignment::getUserId, req.getUserId())
                .eq(TaskAssignment::getStatus, 0));
        if (pendingCount > 0) {
            throw new IllegalArgumentException("您手头还有 " + pendingCount + " 张未完成，请先处理完再抢！");
        }

        // 3. 执行核心查询 (使用上面定义的 SQL)
        ProjectTask project = projectTaskMapper.selectById(req.getTaskId());
        if (project == null) {
            throw new IllegalArgumentException("项目不存在");
        }
        // 默认10次，或者取数据库配置
        int targetCount = (project.getTargetPerAsset() == null) ? 10 : project.getTargetPerAsset();

        // 3. 执行核心查询 (传入 targetCount)
        // 这样数据库就会自动过滤掉 labeled_count >= targetCount 的图
        List<TaskAsset> candidates = taskAssetMapper.findAssetsForGrab(
                req.getTaskId(),
                req.getUserId(),
                req.getCount(),
                targetCount // 传入目标值
        );

        // 4. 异常抛出 (处理 Q2)
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalArgumentException("当前公海池已无图可抢，请稍后再试！");
        }

        // 5. 批量锁定 (入库)
        List<TaskAssignment> newAssignments = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        // 抢单任务有效期 24 小时
        LocalDateTime expireTime = now.plusHours(36);

        for (TaskAsset asset : candidates) {
            TaskAssignment assignment = new TaskAssignment();
            assignment.setTaskId(req.getTaskId());
            assignment.setUserId(req.getUserId());
            assignment.setAssetId(asset.getId());
            assignment.setAssignType("GRAB"); // 抢单类型
            assignment.setStatus(0); // 待标
            assignment.setCreateTime(now);
            assignment.setExpireTime(expireTime); // 修复之前的语法错误
            newAssignments.add(assignment);
        }

        this.saveBatch(newAssignments);

        return newAssignments;
    }
    // TaskServiceImpl.java

    @Transactional(rollbackFor = Exception.class) // 务必加上事务注解！
    public void submitTask(Long userId, SubmitReq req) {
        // 1. 校验权限：检查这个用户有没有锁定这张图，且必须是“待标(0)”状态
        // 防止用户重复提交，或者提交了不属于他的任务
        TaskAssignment assignment = taskAssignmentMapper.selectOne(
                new LambdaQueryWrapper<TaskAssignment>()
                        .eq(TaskAssignment::getUserId, userId)
                        .eq(TaskAssignment::getAssetId, req.getAssetId())
                        .eq(TaskAssignment::getStatus, 0) // 0-待标
                        .eq(TaskAssignment::getTaskId, req.getTaskId())
        );

        if (assignment == null) {
            throw new RuntimeException("任务不存在或已提交，请勿重复操作");
        }
        if (req.getResultData() == null || req.getResultData().isEmpty()) {
            throw new IllegalArgumentException("标注数据不能为空");
        }
        ProjectTask project = projectTaskMapper.selectById(req.getTaskId());
        List<Map<String, Object>> configTemplate = project.getConfig();
        for (Map<String, Object> field : configTemplate) {
            String key = (String) field.get("field");
            Boolean required = (Boolean) field.get("required");
            if (Boolean.TRUE.equals(required) && !req.getResultData().containsKey(key)) {
                throw new IllegalArgumentException("缺少必填字段: " + key);
            }
        }

        // 2. 保存标注结果 (AnnotationRecord)
        AnnotationRecord record = new AnnotationRecord();
        record.setTaskId(req.getTaskId());
        record.setAssetId(req.getAssetId());
        record.setUserId(userId);
        record.setResultData(req.getResultData()); // 这里 MyBatis-Plus 会自动转 JSON 存入
        record.setDuration(req.getTimeCost());
        record.setCreateTime(LocalDateTime.now());
        record.setReviewStatus(0);
        annotationRecordMapper.insert(record);

        // 3. 更新分发记录状态 (TaskAssignment) -> 设为已标(1)
        assignment.setStatus(1);
        assignment.setFinishTime(LocalDateTime.now());
        taskAssignmentMapper.updateById(assignment);
        // 4. 更新素材的大计数器 (TaskAsset) -> 原子+1
        taskAssetMapper.incrementLabeledCount(req.getAssetId());

        // 5. (可选) 检查这张图是不是标满了？如果标满了就把 asset 状态置为完成
        // 假设 targetPerAsset 是 10 (你可以先写死，或者查 project 表获取)
        Integer taget = projectTaskMapper.selectOne(new LambdaQueryWrapper<ProjectTask>().eq(ProjectTask::getId, req.getTaskId())).getTargetPerAsset();
        taskAssetMapper.checkAndCompleteAsset(req.getAssetId(), taget);
    }

    @Override
    public Page<AnnotationRecordVO> queryReviewRecords(ReviewQueryReq req) {
        // 1. 构建分页
        Page<AnnotationRecord> pageParam = new Page<>(req.getPage(), req.getSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<AnnotationRecord> wrapper = new LambdaQueryWrapper<>();

        // 使用 Entity 的 getter 方法引用
        wrapper.eq(req.getTaskId() != null, AnnotationRecord::getTaskId, req.getTaskId());
        wrapper.eq(req.getAssetId() != null, AnnotationRecord::getAssetId, req.getAssetId());
        wrapper.eq(req.getUserId() != null, AnnotationRecord::getUserId, req.getUserId());

        // 修正：使用 getReviewStatus
        wrapper.eq(req.getReviewStatus() != null, AnnotationRecord::getReviewStatus, req.getReviewStatus());

        wrapper.orderByDesc(AnnotationRecord::getCreateTime);

        // 3. 查询
        Page<AnnotationRecord> resultPage = annotationRecordMapper.selectPage(pageParam, wrapper);

        // 4. 转换 VO
        Page<AnnotationRecordVO> voPage = new Page<>();
        BeanUtils.copyProperties(resultPage, voPage, "records");

        List<AnnotationRecordVO> voList = resultPage.getRecords().stream().map(record -> {
            AnnotationRecordVO vo = new AnnotationRecordVO();

            // 核心：这里 copyProperties 会把 record.resultData (Map) 原样复制给 vo.resultData
            BeanUtils.copyProperties(record, vo);

            // 填充图片
            TaskAsset asset = taskAssetMapper.selectById(record.getAssetId());
            if (asset != null) {
                vo.setAssetUrl(asset.getAssetUrl());
            }

            // 填充用户名 (如果有 UserMapper)
            User user = userMapper.selectById(record.getUserId());
            if (user != null) vo.setUsername(user.getUsername());

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Transactional(rollbackFor = Exception.class)
    public void rejectAnnotation(Long recordId, String rejectReason) {
        // 1. 查出这条记录
        AnnotationRecord record = annotationRecordMapper.selectById(recordId);
        if (record == null) return;
        int updateCount = taskAssetMapper.decrementLabeledCount(record.getAssetId());
        if (updateCount == 0) {
            throw new IllegalStateException("该图片计数已为0，无法再驳回！");
        }

        record.setReviewStatus(2);
        annotationRecordMapper.updateById(record);

        // 3. 处理 TaskAssignment (分发记录)
        // 找到当时那条分发记录，将其状态改为 2 (失效/驳回)，这样历史记录还在，但状态不对了
        TaskAssignment assignment = taskAssignmentMapper.selectOne(
                new LambdaQueryWrapper<TaskAssignment>()
                        .eq(TaskAssignment::getUserId, record.getUserId())
                        .eq(TaskAssignment::getAssetId, record.getAssetId())
                        .orderByDesc(TaskAssignment::getId) // 找最新的一条
                        .last("LIMIT 1")
        );

        if (assignment != null) {
            assignment.setStatus(2); // 2 代表被驳回/失效
            taskAssignmentMapper.updateById(assignment);
        }
        taskAssetMapper.decrementLabeledCount(record.getAssetId());


    }

    @Override
    public StatProjectDTO getProjectStats(Long taskId) {
        // 1. 查项目基本信息
        ProjectTask project = projectTaskMapper.selectById(taskId);
        if (project == null) {
            throw new IllegalArgumentException("项目不存在");
        }

        // 2. 统计总图片数
        Long totalAssets = taskAssetMapper.selectCount(
                new LambdaQueryWrapper<TaskAsset>()
                        .eq(TaskAsset::getTaskId, taskId)
        );

        // 3. 统计已完成图片数 (status = 1)
        Long completedAssets = taskAssetMapper.selectCount(
                new LambdaQueryWrapper<TaskAsset>()
                        .eq(TaskAsset::getTaskId, taskId)
                        .eq(TaskAsset::getStatus, 1)
        );

        // 4. 统计已提交的标注记录数
        Long totalAnnotations = annotationRecordMapper.selectCount(
                new LambdaQueryWrapper<AnnotationRecord>()
                        .eq(AnnotationRecord::getTaskId, taskId)
                        .eq(AnnotationRecord::getReviewStatus, 0) // 只统计未被驳回的
        );

        // 5. 计算目标标注数
        int targetPerAsset = (project.getTargetPerAsset() == null) ? 10 : project.getTargetPerAsset();
        Long targetAnnotations = totalAssets * targetPerAsset;

        // 6. 计算进度百分比
        Double progress = (totalAssets == 0) ? 0.0 : (completedAssets * 100.0 / totalAssets);

        // 7. 组装返回
        StatProjectDTO dto = new StatProjectDTO();
        dto.setTaskId(taskId);
        dto.setTaskName(project.getTaskName());
        dto.setTotalAssets(totalAssets.intValue());
        dto.setCompletedAssets(completedAssets.intValue());
        dto.setProgress(Math.round(progress * 100) / 100.0); // 保留两位小数
        dto.setTotalAnnotations(totalAnnotations);
        dto.setTargetAnnotations(targetAnnotations);

        return dto;
    }

    @Override
    public List<StatUserDTO> getUserStats(Long taskId) {
        return annotationRecordMapper.getUserStats(taskId);
    }

    @Override
    public List<ExportRecordDTO> exportDataToExcel(Long taskId, HttpServletResponse response) throws IOException {
        // 1. 查项目配置
        ProjectTask project = projectTaskMapper.selectById(taskId);
        if (project == null) {
            throw new IllegalArgumentException("项目不存在");
        }

        List<Map<String, Object>> configTemplate = project.getConfig();
        if (configTemplate == null || configTemplate.isEmpty()) {
            throw new IllegalArgumentException("项目配置为空，无法导出");
        }

        // 2. 查出所有已完成的图片
        List<TaskAsset> completedAssets = taskAssetMapper.selectList(
                new LambdaQueryWrapper<TaskAsset>()
                        .eq(TaskAsset::getTaskId, taskId)
                        .eq(TaskAsset::getStatus, 1)
        );

        if (completedAssets.isEmpty()) {
            throw new IllegalArgumentException("暂无已完成的标注数据");
        }

        // 3. 聚合数据（核心逻辑）
        List<ExportRecordDTO> exportData = aggregateDataDynamically(completedAssets, configTemplate);

        // 4. 动态生成 Excel 并写入
        writeExcelDynamically(response, exportData, configTemplate, project.getTaskName());
        return exportData;
    }

    // ========== 核心：动态聚合逻辑 ==========
    private List<ExportRecordDTO> aggregateDataDynamically(
            List<TaskAsset> assets,
            List<Map<String, Object>> configTemplate
    ) {
        List<ExportRecordDTO> result = new ArrayList<>();

        for (TaskAsset asset : assets) {
            // 查出这张图的所有有效标注记录
            List<AnnotationRecord> records = annotationRecordMapper.selectList(
                    new LambdaQueryWrapper<AnnotationRecord>()
                            .eq(AnnotationRecord::getAssetId, asset.getId())
                            .in(AnnotationRecord::getReviewStatus, 0, 1) // 未审核或已通过
            );

            if (records.isEmpty()) continue;

            ExportRecordDTO dto = new ExportRecordDTO();
            dto.setAssetUrl(asset.getAssetUrl());
            dto.setLabelCount(records.size());

            // 收集所有人的标注数据
            List<Map<String, Object>> allAnnotations = records.stream()
                    .map(AnnotationRecord::getResultData)
                    .collect(Collectors.toList());

            // 标注者ID列表
            String annotatorIds = records.stream()
                    .map(r -> String.valueOf(r.getUserId()))
                    .collect(Collectors.joining(","));
            dto.setAnnotatorIds(annotatorIds);

            // ========== 关键：根据配置动态聚合每个字段 ==========
            Map<String, Object> aggregatedData = new HashMap<>();

            for (Map<String, Object> fieldConfig : configTemplate) {
                String fieldName = (String) fieldConfig.get("field");
                String fieldType = (String) fieldConfig.get("type");

                Object aggregatedValue;

                // 根据字段类型选择聚合策略
                if ("slider".equals(fieldType)) {
                    // 10分制 → 计算平均值
                    aggregatedValue = getAverage(allAnnotations, fieldName);
                } else if ("select".equals(fieldType) || "radio".equals(fieldType)) {
                    // 分类字段 → 计算众数
                    aggregatedValue = getMostFrequent(allAnnotations, fieldName);
                } else {
                    // 其他类型（如文本输入）→ 取第一个人的值（或者拼接所有人的）
                    aggregatedValue = allAnnotations.get(0).get(fieldName);
                }

                aggregatedData.put(fieldName, aggregatedValue);
            }

            dto.setAggregatedData(aggregatedData);
            result.add(dto);
        }

        return result;
    }

    // ========== 工具方法（复用之前的） ==========
    private Double getAverage(List<Map<String, Object>> allData, String field) {
        double avg = allData.stream()
                .map(data -> data.get(field))
                .filter(val -> val instanceof Number)
                .mapToDouble(val -> ((Number) val).doubleValue())
                .average()
                .orElse(0.0);

        // 保留两位小数
        return Math.round(avg * 100.0) / 100.0;
    }

    private String getMostFrequent(List<Map<String, Object>> allData, String field) {
        Map<String, Long> countMap = allData.stream()
                .map(data -> String.valueOf(data.get(field)))
                .filter(val -> val != null && !"null".equals(val))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return countMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("未知");
    }

    // ========== 动态生成 Excel ==========
    private void writeExcelDynamically(
            HttpServletResponse response,
            List<ExportRecordDTO> data,
            List<Map<String, Object>> configTemplate,
            String projectName
    ) throws IOException {

        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("标注结果_" + projectName, "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        // 使用 EasyExcel 的动态表头功能
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
        WriteSheet writeSheet = EasyExcel.writerSheet("标注结果").build();

        // 1. 构建表头（动态列）
        List<List<String>> head = new ArrayList<>();
        head.add(Collections.singletonList("图片URL"));
        head.add(Collections.singletonList("标注次数"));
        head.add(Collections.singletonList("标注者ID"));

        // 根据配置动态添加列
        for (Map<String, Object> fieldConfig : configTemplate) {
            String label = (String) fieldConfig.get("label");
            String type = (String) fieldConfig.get("type");

            // 根据类型添加后缀说明
            String columnName = label;
            if ("slider".equals(type)) {
                columnName += "(平均分)";
            } else if ("select".equals(type) || "radio".equals(type)) {
                columnName += "(众数)";
            }

            head.add(Collections.singletonList(columnName));
        }

        // 2. 构建数据行
        List<List<Object>> dataList = new ArrayList<>();
        for (ExportRecordDTO record : data) {
            List<Object> row = new ArrayList<>();
            row.add(record.getAssetUrl());
            row.add(record.getLabelCount());
            row.add(record.getAnnotatorIds());

            // 按配置顺序添加聚合数据
            for (Map<String, Object> fieldConfig : configTemplate) {
                String fieldName = (String) fieldConfig.get("field");
                Object value = record.getAggregatedData().get(fieldName);
                row.add(value != null ? value : "");
            }

            dataList.add(row);
        }

        // 3. 写入 Excel
        excelWriter.write(dataList,
                EasyExcel.writerSheet("标注结果")
                        .head(head)  // 设置表头
                        .build()
        );
        excelWriter.finish();
    }




}
