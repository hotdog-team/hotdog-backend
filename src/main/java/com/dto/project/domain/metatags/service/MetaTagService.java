package com.dto.project.domain.metatags.service;

import com.dto.project.domain.metatags.dto.MetaTagDTO;
import com.dto.project.domain.metatags.entity.MetaTagEntity;
import com.dto.project.domain.metatags.entity.MetaTagStatus;
import com.dto.project.domain.metatags.repository.MetaTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Primary
@Transactional
public class MetaTagService {

    @Autowired
    MetaTagRepository metaTagRepository;

    @Transactional(readOnly = true)
    public List<MetaTagDTO> listAllMetaTag() {
        return metaTagRepository.findAllByStatus(MetaTagStatus.ACTIVE).stream()
                .map(MetaTagDTO::toDTO)
                .toList();
    }

    public void insertMetaTag(MetaTagDTO mtgVo) {
        MetaTagEntity entity = MetaTagEntity.toEntity(mtgVo);
        entity.setStatus(MetaTagStatus.ACTIVE);
        entity.setUpdatedAt(LocalDateTime.now());
        metaTagRepository.save(entity);
    }

    public void updateMetaTag(MetaTagDTO mtgVo) {
        MetaTagEntity entity = MetaTagEntity.toEntity(mtgVo);
        metaTagRepository.save(entity);
    }

    public void deleteMetaTag(int id) {
        MetaTagEntity entity = metaTagRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "MetaTag not found: " + id));
        entity.setStatus(MetaTagStatus.DELETED);
        entity.setUpdatedAt(LocalDateTime.now());
        metaTagRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public MetaTagDTO detailViewMetaTag(int id) {
        MetaTagEntity entity = metaTagRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "MetaTag not found: " + id));
        return MetaTagDTO.toDTO(entity);
    }
}
