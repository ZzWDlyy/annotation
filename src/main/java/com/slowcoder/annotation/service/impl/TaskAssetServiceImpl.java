package com.slowcoder.annotation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slowcoder.annotation.entity.TaskAsset;
import com.slowcoder.annotation.mapper.TaskAssetMapper;
import com.slowcoder.annotation.service.TaskAssetService;
import org.springframework.stereotype.Service;

@Service
public class TaskAssetServiceImpl extends ServiceImpl<TaskAssetMapper, TaskAsset> implements TaskAssetService {
    // 这里什么都不用写，父类已经帮你实现了 saveBatch, getById, update 等所有方法
}
