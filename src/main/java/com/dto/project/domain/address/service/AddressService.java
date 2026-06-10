package com.dto.project.domain.address.service;

import com.dto.project.domain.address.dto.AddressCreateRequest;
import com.dto.project.domain.address.dto.AddressResponse;
import com.dto.project.domain.address.dto.AddressUpdateRequest;
import com.dto.project.domain.address.entity.Address;
import com.dto.project.domain.address.repository.AddressRepository;
import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {

    private final AddressRepository addressRepository;
    private final MemberRepository memberRepository;

    // 배송지 등록
    @Transactional
    public AddressResponse createAddress(Long memberId, AddressCreateRequest request) {

        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 기존 배송지 존재 여부 확인
        boolean hasAddress = addressRepository.existsByMemberIdAndStatus(memberId, "ACTIVE");

        // 기본 배송지 여부 결정
        boolean isDefault = Boolean.TRUE.equals(request.getIsDefault()) || !hasAddress;

        // 기본 배송지로 등록하는 경우 기존 기본 배송지 해제
        if (isDefault) {
            clearDefaultAddress(memberId);
        }

        // 배송지 Entity 생성
        Address address = Address.builder()
                .member(member)
                .addressName(request.getAddressName())
                .zipCode(request.getZipCode())
                .baseAddress(request.getBaseAddress())
                .detailAddress(request.getDetailAddress())
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .isDefault(isDefault)
                .status("ACTIVE")
                .build();

        // 배송지 저장
        Address savedAddress = addressRepository.save(address);

        // 응답 반환
        return AddressResponse.from(savedAddress);
    }

    // 내 배송지 목록 조회
    public List<AddressResponse> getMyAddresses(Long memberId) {

        return addressRepository.findAllByMemberIdAndStatusOrderByCreatedAtDesc(memberId, "ACTIVE")
                .stream()
                .map(AddressResponse::from)
                .toList();
    }

    // 배송지 단건 조회
    public AddressResponse getAddress(Long memberId, Long addressId) {

        Address address = addressRepository.findByIdAndMemberIdAndStatus(addressId, memberId, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("배송지를 찾을 수 없습니다."));

        return AddressResponse.from(address);
    }

 // 배송지 수정
    @Transactional
    public AddressResponse updateAddress(Long memberId, Long addressId, AddressUpdateRequest request) {

        // 배송지 조회
        Address address = addressRepository.findByIdAndMemberIdAndStatus(addressId, memberId, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("배송지를 찾을 수 없습니다."));

        // 기본 배송지로 변경하는 경우 기존 기본 배송지 해제
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultAddress(memberId);
            address.setDefault(true);
        }

        // 주소 정보 수정
        address.updateAddress(
                request.getAddressName(),
                request.getZipCode(),
                request.getBaseAddress(),
                request.getDetailAddress(),
                request.getReceiverName(),
                request.getReceiverPhone()
        );

        return AddressResponse.from(address);
    }

    // 배송지 삭제
    @Transactional
    public void deleteAddress(Long memberId, Long addressId) {

        Address address = addressRepository.findByIdAndMemberIdAndStatus(addressId, memberId, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("배송지를 찾을 수 없습니다."));

        address.deleteAddress();
    }

    // 기존 기본 배송지 해제
    private void clearDefaultAddress(Long memberId) {

    	addressRepository.findAllByMemberIdAndIsDefaultTrueAndStatus(memberId, "ACTIVE")
        .forEach(address -> address.setDefault(false));
    }
}