package com.dto.project.domain.address.controller;

import com.dto.project.domain.address.dto.AddressCreateRequest;
import com.dto.project.domain.address.dto.AddressResponse;
import com.dto.project.domain.address.dto.AddressUpdateRequest;
import com.dto.project.domain.address.service.AddressService;
import com.dto.project.global.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/addresses")
public class AddressController {

    private final AddressService addressService;
    private final SecurityUtil securityUtil;

    // 배송지 등록
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse createAddress(@RequestBody AddressCreateRequest request) {
    	Long memberId = securityUtil.resolveMemberId();
        return addressService.createAddress(memberId, request);
    }

    // 내 배송지 목록 조회
    @GetMapping
    public List<AddressResponse> getMyAddresses() {
        Long memberId = securityUtil.resolveMemberId();
        return addressService.getMyAddresses(memberId);
    }

    // 배송지 단건 조회
    @GetMapping("/{id}")
    public AddressResponse getAddress(@PathVariable Long id) {
        Long memberId = securityUtil.resolveMemberId();
        return addressService.getAddress(memberId, id);
    }
    
 // 배송지 수정
    @PatchMapping("/{id}")
    public AddressResponse updateAddress(
            @PathVariable Long id,
            @RequestBody AddressUpdateRequest request
    ) {
        Long memberId = securityUtil.resolveMemberId();
        return addressService.updateAddress(memberId, id, request);
    }

    // 기본 배송지 설정
    @PatchMapping("/{id}/default")
    public AddressResponse setDefaultAddress(@PathVariable Long id) {
        Long memberId = securityUtil.resolveMemberId();
        return addressService.setDefaultAddress(memberId, id);
    }

    // 배송지 삭제
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(@PathVariable Long id) {
        Long memberId = securityUtil.resolveMemberId();
        addressService.deleteAddress(memberId, id);
    }
}
