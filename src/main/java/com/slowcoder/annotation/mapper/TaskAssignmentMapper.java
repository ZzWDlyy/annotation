package com.slowcoder.annotation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.slowcoder.annotation.entity.TaskAssignment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskAssignmentMapper extends BaseMapper<TaskAssignment> {
}
