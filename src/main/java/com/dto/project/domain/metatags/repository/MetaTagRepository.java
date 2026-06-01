package com.dto.project.domain.metatags.repository;

import com.dto.project.domain.metatags.entity.MetaTagEntity;
import com.dto.project.domain.metatags.entity.MetaTagType;
import com.dto.project.domain.metatags.entity.MetaTagStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MetaTagRepository extends JpaRepository<MetaTagEntity, Long> {
    List<MetaTagEntity> findAllByMetaTagStatus(MetaTagStatus metaTagStatus);

    Optional<MetaTagEntity> findByNameAndType(String name, MetaTagType type);
}
