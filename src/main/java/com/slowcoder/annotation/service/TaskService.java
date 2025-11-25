package com.slowcoder.annotation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.slowcoder.annotation.dto.*;
import com.slowcoder.annotation.entity.TaskAssignment;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public interface TaskService {
    public boolean assignTasks(AssignTaskReq req);
    public List<TaskAssignment> getMyList();

    public List<TaskAssignment> grabTask(TaskGrabReq req);

    public void submitTask(Long userId, SubmitReq req);

    Page<AnnotationRecordVO> queryReviewRecords(ReviewQueryReq req);

    public void rejectAnnotation(Long recordId, String rejectReason);

    StatProjectDTO getProjectStats(Long taskId);

    List<StatUserDTO> getUserStats(Long taskId);

    List<ExportRecordDTO> exportDataToExcel(Long taskId, HttpServletResponse response) throws IOException;

}
