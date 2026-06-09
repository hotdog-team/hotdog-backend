package com.dto.project.domain.admin.product.service;

import com.dto.project.domain.admin.product.dto.NaverProductCreateRequest;
import com.dto.project.domain.admin.product.dto.NaverShoppingResponse;
import com.dto.project.domain.product.entity.Product;
import com.dto.project.domain.product.entity.ProductImage;
import com.dto.project.domain.product.repository.ProductImageRepository;
import com.dto.project.domain.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class NaverShoppingService {
	
	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;

    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.client-secret}")
    private String clientSecret;

    @Value("${naver.api.shopping-url}")
    private String shoppingUrl;
    
    // 네이버 쇼핑 상품 검색
    public NaverShoppingResponse searchProducts(String query) {
    	// 네이버 쇼핑 검색 API 요청 URL 생성
    	String url = UriComponentsBuilder
    	        .fromUriString(shoppingUrl)
    	        .queryParam("query", query)
    	        .queryParam("display", 10)
    	        .queryParam("start", 1)
    	        .queryParam("sort", "sim")
    	        .build()
    	        .toUriString();
    	// 네이버 쇼핑 API 호출
        return RestClient.create()
                .get()
                .uri(url)
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .retrieve()
                .body(NaverShoppingResponse.class);
    }
    
    // 네이버 쇼핑 API 응답의 HTML 태그 제거
    private String removeHtmlTags(String text) {
        return text == null ? null : text.replaceAll("<[^>]*>", "");
    }
    
    // 네이버 쇼핑 상품 등록
    @Transactional
    public void createProductFromNaver(
            NaverProductCreateRequest request
    ) {

        Product product = new Product();

        product.updateProductInfo(
                request.getCategoryId(),
                removeHtmlTags(request.getTitle()),
                request.getLprice(),
                0,
                100,
                "네이버 쇼핑 API 등록 상품",
                request.getLink(),
                request.getBrand(),
                request.getMallName(),
                null,
                removeHtmlTags(request.getTitle())
        );

        product.changeStatus("ON_SALE");

        Product savedProduct = productRepository.save(product);

        if (request.getImage() != null && !request.getImage().isBlank()) {
            ProductImage productImage = new ProductImage();
            productImage.setProductId(savedProduct.getId());
            productImage.setImageUrl(request.getImage());
            productImage.setIsMain("Y");

            productImageRepository.save(productImage);
        }
    }
    
}