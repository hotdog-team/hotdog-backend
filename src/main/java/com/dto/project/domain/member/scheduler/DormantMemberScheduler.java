package com.dto.project.domain.member.scheduler;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.member.entity.MemberStatus;
import com.dto.project.domain.member.repository.MemberRepository;
import com.dto.project.domain.weighting.service.MemberTagWeightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DormantMemberScheduler {

    private final MemberRepository memberRepository;
    private final MemberTagWeightService memberTagWeightService;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void processDormantMembersAndWeights() {
        log.info("[Member Scheduler] 휴면 회원 전환 및 만료 가중치 데이터 삭제 배치를 시작합니다.");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneYearAgo = now.minusYears(1);

        // 휴면 상태 전인 Member 가져오기(휴면 상태 처리 직전)
        List<Member> targets = memberRepository
                .findAllByStatusAndLastLoginAtBefore(MemberStatus.ACTIVE, oneYearAgo);

        // 먼저 memberTagWeightService에 있는 내용을 삭제한다
        for (Member member : targets) {
            memberTagWeightService.clearBehaviorOnDormant(member);
        }

        // 1년 미접속 회원 -> 휴면(DORMANT) 상태로 변경
        int updatedDormantCount = memberRepository.updateDormantMembers(oneYearAgo);
        log.info("[Member Scheduler] 휴면 전환 완료: {} 명", updatedDormantCount);


    }
}