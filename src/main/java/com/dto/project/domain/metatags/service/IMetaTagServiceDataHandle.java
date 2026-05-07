package com.dto.project.domain.metatags.service;

import com.dto.project.domain.metatags.entity.MetaTagEntity;

import java.util.List;
import java.util.Optional;

public interface IMetaTagServiceDataHandle {
    List<MetaTagEntity> listAllMetaTag();
    void insertMetaTag(MetaTagEntity entity); //메타태그 등록
    void updateMetaTag(MetaTagEntity entity); //메타태그 업데이트
    void deleteMetaTag(MetaTagEntity entity); //메타태그 삭제(soft-delete)
    Optional<MetaTagEntity> detailViewMetaTag(int id); //메타태그 조회
}
