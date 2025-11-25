package com.slowcoder.annotation.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.slowcoder.annotation.dto.StatUserDTO;
import com.slowcoder.annotation.entity.AnnotationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnnotationRecordMapper extends BaseMapper<AnnotationRecord> {
    List<StatUserDTO> getUserStats(@Param("taskId") Long taskId);
}
