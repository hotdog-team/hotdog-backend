package com.dto.project.domain.metatags.service;

import com.dto.project.domain.metatags.dto.MetaTagDTO;

import java.util.List;

public interface IMetaTagService {
    List<MetaTagDTO> listAllMetaTag(); //메타태그 목록 조회
    void insertMetaTag(MetaTagDTO mtgVo); //메타태그 등록
    void updateMetaTag(MetaTagDTO mtgVo); //메타태그 업데이트
    void deleteMetaTag(int id); //메타태그 삭제
    MetaTagDTO detailViewMetaTag(int id); //메타태그 detail
}
