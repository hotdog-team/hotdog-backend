package com.dto.project.domain.faq.repository;

import com.dto.project.domain.faq.entity.Faq;
import com.dto.project.domain.faq.entity.FaqCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long> {
    List<Faq> findByCategoryAndStatus(FaqCategory category, String status);
    List<Faq> findByStatus(String status);
}