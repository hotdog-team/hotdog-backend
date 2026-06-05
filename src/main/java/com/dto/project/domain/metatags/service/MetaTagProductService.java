package com.dto.project.domain.metatags.service;

import com.dto.project.domain.metatags.dto.MetaTagResponse;
import com.dto.project.domain.metatags.entity.MetaTagProduct;
import com.dto.project.domain.metatags.entity.MetaTagStatus;
import com.dto.project.domain.metatags.repository.MetaTagProductRepository;
import com.dto.project.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.*;


@Service
@RequiredArgsConstructor
public class MetaTagProductService {

    private final MetaTagProductRepository metaTagProductRepository;
    private final ProductRepository productRepository;

    //상품 아이디 통하여 해당하는 metaTag를 반환합니다.
    @Transactional(readOnly = true)
    public List<MetaTagResponse> findAllByProductId(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "해당 상품을 찾을 수 없습니다."));

        List<MetaTagProduct> mappings = metaTagProductRepository.findByProduct_Id(productId);

        return mappings.stream()
                .map(MetaTagProduct::getMetaTag)
                .filter(tag -> tag.getMetaTagStatus() == MetaTagStatus.ACTIVE)
                .map(MetaTagResponse::from)
                .toList();
    }

}
