package com.slowcoder.annotation.service.impl;

import com.slowcoder.annotation.dto.AssetImportReq;
import com.slowcoder.annotation.dto.ProjectCreateReq;
import com.slowcoder.annotation.entity.ProjectTask;
import com.slowcoder.annotation.entity.TaskAsset;
import com.slowcoder.annotation.mapper.ProjectTaskMapper;
import com.slowcoder.annotation.service.ProjectService;
import com.slowcoder.annotation.service.TaskAssetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {
    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectTaskMapper projectTaskMapper;

    private final TaskAssetService taskAssetService;

    public ProjectServiceImpl(ProjectTaskMapper projectTaskMapper, TaskAssetService taskAssetService) {
        this.projectTaskMapper = projectTaskMapper;
        this.taskAssetService = taskAssetService;
        logger.info("ProjectServiceImpl初始化完成");
    }


    @Override
    public Long createProject(ProjectCreateReq req) {
        logger.debug("正在创建任务: {}", req.getTaskName());

        // --- 鲁棒性升级：业务逻辑校验 ---
        // 检查题目配置是否合法 (例如：如果是滑块题，必须有最大最小值)
        validateConfig(req.getConfig());

        // 1. 创建实体对象
        ProjectTask task = new ProjectTask();
        task.setTaskName(req.getTaskName());
        task.setDescription(req.getDescription());
        task.setMediaType(req.getMediaType());
        task.setTargetPerAsset(req.getTargetPerAsset());
        task.setConfigTemplate(req.getConfig());
        task.setStatus(1);

        // 2. 写入数据库
        projectTaskMapper.insert(task);

        logger.info("任务创建成功，ID: {}", task.getId());
        return task.getId();
    }

    @Override
    public boolean importAssets(AssetImportReq req) {
        // 1. 基础校验
        if (req.getProjectId() == null) {
            throw new IllegalArgumentException("项目id不能为空");
        }
        if (req.getUrls() == null || req.getUrls().isEmpty()) {
            throw new IllegalArgumentException("导入列表不能为空");
        }


        // 2. 转换对象 (这里是在内存中操作，速度很快)
        List<TaskAsset> taskAssets = new ArrayList<>(req.getUrls().size());
        for (String url : req.getUrls()) {
            TaskAsset taskAsset = new TaskAsset();
            taskAsset.setAssetUrl(url);
            taskAsset.setTaskId(req.getProjectId());
            taskAsset.setLabeledCount(0);
            taskAsset.setStatus(0);
            taskAssets.add(taskAsset);
        }


        boolean success = taskAssetService.saveBatch(taskAssets, 2000);

        logger.info("项目[{}] 批量导入完成: {}, 总数量: {}", req.getProjectId(), success, taskAssets.size());
        return success;
    }

    private void validateConfig(List<ProjectTask.TaskItem> config) {
        if (config == null || config.isEmpty()) {
            // 这里抛出 RuntimeException，稍后我们用全局异常处理器拦截它
            throw new IllegalArgumentException("题目配置不能为空");
        }
        for (ProjectTask.TaskItem item : config) {
            if (item.getKey() == null || item.getLabel() == null) {
                throw new IllegalArgumentException("题目配置缺少 key 或 label");
            }
            if ("slider".equals(item.getType())) {
                if (item.getMin() == null || item.getMax() == null) {
                    throw new IllegalArgumentException("滑块题目必须设置 min 和 max");
                }
            }
        }
    }

}