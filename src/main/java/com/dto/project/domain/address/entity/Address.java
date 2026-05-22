package com.dto.project.domain.address.entity;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.global.entity.BaseTimeEntity; // 공통 상속 엔티티
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "addresses")
public class Address extends BaseTimeEntity { // 생성/수정 시간 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회원이 삭제되면 배송지도 삭제되도록 설정 (연관관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "address_name", length = 100)
    private String addressName;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @Column(name = "base_address", length = 255)
    private String baseAddress;

    @Column(name = "detail_address", length = 255)
    private String detailAddress;

    @Column(name = "receiver_name", length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", length = 50)
    private String receiverPhone;

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Builder.Default
    @Column(length = 20, nullable = false)
    private String status = "ACTIVE"; // ACTIVE, DELETED

    // ==========================================
    // 비즈니스 로직 메서드
    // ==========================================

    /**
     * 주소 정보 업데이트
     */
    public void updateAddress(String zipCode, String baseAddress, String detailAddress) {
        if (zipCode != null) this.zipCode = zipCode;
        if (baseAddress != null) this.baseAddress = baseAddress;
        if (detailAddress != null) this.detailAddress = detailAddress;
    }

    /**
     * 기본 배송지 설정/해제
     */
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}