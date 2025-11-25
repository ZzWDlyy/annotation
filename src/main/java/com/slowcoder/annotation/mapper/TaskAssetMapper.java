package com.slowcoder.annotation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.slowcoder.annotation.entity.TaskAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;


@Mapper
public interface TaskAssetMapper extends BaseMapper<TaskAsset>  {
    List<TaskAsset> findAssetsForGrab(
            @Param("taskId") Long taskId,
            @Param("userId") Long userId,
            @Param("limit") Integer limit,
            @Param("targetCount") Integer targetCount // 新增：目标次数 (例如 10)
    );

    // TaskAssetMapper.java

    // 原子更新：直接在数据库层面 +1，线程安全
    @Update("UPDATE task_asset SET labeled_count = labeled_count + 1 WHERE id = #{assetId}")
    void incrementLabeledCount(@Param("assetId") Long assetId);

    // 检查并更新完成状态 (如果达到目标数，标记为完成)
    @Update("UPDATE task_asset SET status = 1 WHERE id = #{assetId} AND labeled_count >= #{target}")
    void checkAndCompleteAsset(@Param("assetId") Long assetId, @Param("target") Integer target);

    // 回退/驳回时使用：计数器减1，且如果状态是完成(1)，强制变回进行中(0)
    @Update("UPDATE task_asset SET labeled_count = labeled_count - 1, status = 0 WHERE id = #{assetId} AND labeled_count > 0")
    int decrementLabeledCount(@Param("assetId") Long assetId);


}
