package com.dto.project.domain.metatags.service;

import com.dto.project.domain.metatags.dto.MetaTagDTO;
import com.dto.project.domain.metatags.entity.MetaTagEntity;
import com.dto.project.domain.metatags.entity.MetaTagStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Primary
public class MetaTagService implements IMetaTagService {
    @Autowired
    IMetaTagServiceDataHandle metaTagServiceDataHandle;

    @Override
    public List<MetaTagDTO> listAllMetaTag() {
        List<MetaTagEntity> entityList = metaTagServiceDataHandle.listAllMetaTag();
        List<MetaTagDTO> dtoList = new ArrayList<MetaTagDTO>();

        for(MetaTagEntity entity:entityList){
            MetaTagDTO dto = MetaTagDTO.toDTO(entity);
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Override
    public void insertMetaTag(MetaTagDTO mtgVo) {
        MetaTagEntity entity = MetaTagEntity.toEntity(mtgVo);
        //기본값 1차례 설정
        entity.setStatus(MetaTagStatus.ACTIVE);
        entity.setUpdatedAt(LocalDateTime.now());
        metaTagServiceDataHandle.insertMetaTag(entity);
    }

    @Override
    public void updateMetaTag(MetaTagDTO mtgVo) {
        MetaTagEntity entity = MetaTagEntity.toEntity(mtgVo);
        metaTagServiceDataHandle.updateMetaTag(entity);
    }

    @Override
    public void deleteMetaTag(int id) {
        MetaTagEntity entity = metaTagServiceDataHandle.detailViewMetaTag(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "MetaTag not found: " + id));
        entity.setStatus(MetaTagStatus.DELETED);
        entity.setUpdatedAt(LocalDateTime.now());

        metaTagServiceDataHandle.deleteMetaTag(entity);
    }

    @Override
    public MetaTagDTO detailViewMetaTag(int id) {
        MetaTagEntity entity = metaTagServiceDataHandle.detailViewMetaTag(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "MetaTag not found: " + id));

        return MetaTagDTO.toDTO(entity);
    }
}
