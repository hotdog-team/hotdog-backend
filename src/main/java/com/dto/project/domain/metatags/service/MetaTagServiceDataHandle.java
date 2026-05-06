package com.dto.project.domain.metatags.service;

import com.dto.project.domain.metatags.entity.MetaTagEntity;
import com.dto.project.domain.metatags.entity.MetaTagStatus;
import com.dto.project.domain.metatags.repository.MetaTagRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Primary
@Transactional
public class MetaTagServiceDataHandle implements IMetaTagServiceDataHandle {
    @Autowired
    MetaTagRepository mtgRepo;

    @Override
    public List<MetaTagEntity> listAllMetaTag() {
        return mtgRepo.findAllByStatus(MetaTagStatus.ACTIVE);
    }

    @Override
    public void insertMetaTag(MetaTagEntity entity) {
        mtgRepo.save(entity);
    }

    @Override
    public void updateMetaTag(MetaTagEntity entity) {
        mtgRepo.save(entity);
    }

    @Override
    public void deleteMetaTag(MetaTagEntity entity) {
        //soft-delete됨 메서드 나눔
        mtgRepo.save(entity);
    }

    @Override
    public Optional<MetaTagEntity> detailViewMetaTag(int id) {
        return mtgRepo.findById(id);
    }
}
