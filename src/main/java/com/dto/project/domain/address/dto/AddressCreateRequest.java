package com.dto.project.domain.address.dto;

import lombok.Getter;

@Getter
public class AddressCreateRequest {

    // 우편번호
    private String zipCode;

    // 기본 주소
    private String baseAddress;

    // 상세 주소
    private String detailAddress;

    // 수령인 이름
    private String receiverName;

    // 수령인 전화번호
    private String receiverPhone;

    // 기본 배송지 여부
    private Boolean isDefault;
}
