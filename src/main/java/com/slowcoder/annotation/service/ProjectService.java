package com.slowcoder.annotation.service;

import com.slowcoder.annotation.dto.AssetImportReq;
import com.slowcoder.annotation.dto.ProjectCreateReq;

public interface ProjectService {
    Long createProject(ProjectCreateReq req);

    boolean importAssets(AssetImportReq req);
}
