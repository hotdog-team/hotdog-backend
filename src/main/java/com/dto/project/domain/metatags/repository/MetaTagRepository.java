package com.dto.project.domain.metatags.repository;

import com.dto.project.domain.metatags.entity.MetaTagEntity;
import com.dto.project.domain.metatags.entity.MetaTagStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetaTagRepository extends JpaRepository<MetaTagEntity, Long> {
    List<MetaTagEntity> findAllByStatus(MetaTagStatus status);
}
