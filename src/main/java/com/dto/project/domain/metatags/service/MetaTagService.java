package com.dto.project.domain.metatags.service;

import com.dto.project.domain.metatags.dto.MetaTagRequest;
import com.dto.project.domain.metatags.dto.MetaTagResponse;
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
    public List<MetaTagResponse> listAllMetaTag() {
        return metaTagRepository.findAllByMetaTagStatus(MetaTagStatus.ACTIVE).stream()
                .map(MetaTagResponse::from)
                .toList();
    }

    public void insertMetaTag(MetaTagRequest request) {
        MetaTagEntity entity = MetaTagEntity.toEntity(request);
        entity.setMetaTagStatus(MetaTagStatus.ACTIVE);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        metaTagRepository.save(entity);
    }

    public void updateMetaTag(Long id, MetaTagRequest request) {
        MetaTagEntity entity = metaTagRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "해당 메타 태그를 찾을 수 없습니다."));
        entity.setName(request.getName());
        entity.setType(request.getType());
        if (request.getMetaTagStatus() != null) {
            entity.setMetaTagStatus(request.getMetaTagStatus());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        metaTagRepository.save(entity);
    }

    public void deleteMetaTag(Long id) {
        MetaTagEntity entity = metaTagRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "해당 메타 태그를 찾을 수 없습니다."));
        entity.setMetaTagStatus(MetaTagStatus.DELETED);
        entity.setUpdatedAt(LocalDateTime.now());
        metaTagRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public MetaTagResponse detailViewMetaTag(Long id) {
        MetaTagEntity entity = metaTagRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "해당 메타 태그를 찾을 수 없습니다."));
        return MetaTagResponse.from(entity);
    }
}
