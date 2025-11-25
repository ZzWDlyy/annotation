package com.slowcoder.annotation;

import com.slowcoder.annotation.entity.ProjectTask;
import com.slowcoder.annotation.entity.TaskAsset;
import com.slowcoder.annotation.mapper.ProjectTaskMapper;
import com.slowcoder.annotation.mapper.TaskAssetMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class ProjectTest {
    @Autowired
    private ProjectTaskMapper projectTaskMapper;
    @Autowired
    private TaskAssetMapper taskAssetMapper;
    @Test
    void testCreateProjectWithJson() {
        System.out.println("========== 开始测试 JSON 存储 ==========");

        // 1. 创建一个项目任务对象
        ProjectTask task = new ProjectTask();
        task.setTaskName("2024自动驾驶路况标注");
        task.setDescription("请标注图中的红绿灯和车道线，并给清晰度打分");
        task.setMediaType("image");
        task.setTargetPerAsset(10); // 每张图10人标
        task.setStatus(1);

        // 2. 核心：构造复杂的 JSON 配置 (模拟前端传来的题目)
        List<ProjectTask.TaskItem> config = new ArrayList<>();

        // 题目A：滑块打分
        ProjectTask.TaskItem item1 = new ProjectTask.TaskItem();
        item1.setKey("quality_score");
        item1.setLabel("图片清晰度(1-10)");
        item1.setType("slider");
        item1.setMin(1);
        item1.setMax(10);
        config.add(item1);

        // 题目B：单选
        ProjectTask.TaskItem item2 = new ProjectTask.TaskItem();
        item2.setKey("weather");
        item2.setLabel("天气情况");
        item2.setType("radio");
        // 这里演示 List<Object> 存字符串
        item2.setOptions(Arrays.asList("晴天", "雨天", "雪天"));
        config.add(item2);

        // 放入实体
        task.setConfigTemplate(config);

        // 3. 插入数据库
        int result = projectTaskMapper.insert(task);
        System.out.println("插入结果: " + (result > 0 ? "成功" : "失败"));
        System.out.println("生成的任务ID: " + task.getId());

        // 4. 立即查出来，验证 JSON 是否自动转回了 Java 对象
        ProjectTask taskFromDb = projectTaskMapper.selectById(task.getId());

        System.out.println("========== 读取验证 ==========");
        System.out.println("任务名称: " + taskFromDb.getTaskName());

        // 这一步如果不报错，说明 JacksonTypeHandler 生效了！
        List<ProjectTask.TaskItem> template = taskFromDb.getConfigTemplate();
        System.out.println("题目数量: " + template.size());
        System.out.println("第一题类型: " + template.get(0).getType());
        System.out.println("第二题选项: " + template.get(1).getOptions());
    }

    @Test
    void testAddAssets() {
        // 顺便测试一下添加素材
        TaskAsset asset = new TaskAsset();
        asset.setTaskId(1L); // 假设关联 ID=1 的任务
        asset.setAssetUrl("https://img.example.com/demo.jpg");
        asset.setLabeledCount(0);
        asset.setStatus(0);

        taskAssetMapper.insert(asset);
        System.out.println("素材插入成功 ID: " + asset.getId());
    }
}
