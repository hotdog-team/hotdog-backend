package com.dto.project.domain.metatags.service;

import com.dto.project.domain.metatags.dto.MetaTagResponse;
import com.dto.project.domain.metatags.entity.MetaTagProduct;
import com.dto.project.domain.metatags.entity.MetaTagStatus;
import com.dto.project.domain.metatags.repository.MetaTagProductRepository;
import com.dto.project.domain.metatags.repository.MetaTagRepository;
import com.dto.project.domain.product.entity.Product;
import com.dto.project.domain.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MetaTagProductService {
    @Autowired
    MetaTagProductRepository metaTagProductRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    MetaTagRepository metaTagRepository;

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

    //메타태그를 다량으로 삭제-추가합니다.
    //상품 등록-수정시에 사용합니다.
    @Transactional
    public void replaceMetaTagsForProduct(Long productId, List<Long> metaTagIds) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "해당 상품을 찾을 수 없습니다."));

        //입력된 metaTagIds가 하나도 없을 경우 delete 처리만하고 종료.
        if (metaTagIds.isEmpty()) {
            metaTagProductRepository.deleteByProduct_Id(productId);
            return;
        }

        metaTagProductRepository.deleteByProduct_Id(productId);
        Product productRef = productRepository.getReferenceById(productId);
        LocalDateTime now = LocalDateTime.now();
        List<MetaTagProduct> rows = metaTagIds.stream()
                .map(tagId -> MetaTagProduct.builder()
                        .product(productRef)
                        .metaTag(metaTagRepository.getReferenceById(tagId))
                        .createdAt(now)
                        .updatedAt(now)
                        .build())
                .toList();
        metaTagProductRepository.saveAll(rows);
    }
}
