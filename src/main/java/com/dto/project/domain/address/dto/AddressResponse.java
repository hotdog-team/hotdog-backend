package com.dto.project.domain.address.dto;

import com.dto.project.domain.address.entity.Address;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressResponse {
	
    // 배송지 ID
    private Long addressId;

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

    // Entity -> DTO 변환
    public static AddressResponse from(Address address) {
        return AddressResponse.builder()
                .addressId(address.getId())
                .zipCode(address.getZipCode())
                .baseAddress(address.getBaseAddress())
                .detailAddress(address.getDetailAddress())
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .isDefault(address.isDefault())
                .build();
    }
}