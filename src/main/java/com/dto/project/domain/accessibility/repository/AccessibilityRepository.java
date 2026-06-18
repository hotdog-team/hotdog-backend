package com.dto.project.domain.accessibility.repository;

import com.dto.project.domain.accessibility.entity.Accessibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccessibilityRepository extends JpaRepository<Accessibility, Long> {

    Optional<Accessibility> findByMemberId(Long memberId);
}
