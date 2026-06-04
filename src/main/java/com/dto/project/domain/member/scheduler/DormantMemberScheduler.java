package com.dto.project.domain.member.scheduler;

import com.dto.project.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DormantMemberScheduler {

    private final MemberRepository memberRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void processDormantMembersAndWeights() {
        log.info("[Member Scheduler] 휴면 회원 전환 및 만료 가중치 데이터 삭제 배치를 시작합니다.");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneYearAgo = now.minusYears(1);
        LocalDateTime oneAndHalfYearsAgo = now.minusMonths(18);

        // 1년 미접속 회원 -> 휴면(DORMANT) 상태로 변경
        int updatedDormantCount = memberRepository.updateDormantMembers(oneYearAgo);
        log.info("[Member Scheduler] 휴면 전환 완료: {} 명", updatedDormantCount);


    }
}