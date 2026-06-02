package com.dto.project.domain.weighting.scheduler;

import com.dto.project.domain.weighting.service.MemberTagWeightHotService;
import com.dto.project.domain.weighting.service.ProductWeightLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActionLogFlushScheduler {

    //1 트랜잭션 = 500건으로 설정(팀내 policy 의거)
    private static final int MAX_PER_RUN = 500;

    private final MemberTagWeightHotService memberTagWeightHotService;
    private final ProductWeightLogService productWeightLogService;

    //매일 새벽 2시에 일괄 등록(batch)하도록 처리(팀내 policy에 의거)
    @Scheduled(cron = "0 0 2 * * *")
    public void behaviorFlush() {
        int totalBehavior = 0;

        while (true) {
            int queue = productWeightLogService.persistBehaviorLogsFromQueue(MAX_PER_RUN);
            if (queue == 0) break;

            totalBehavior += queue;
        }

        int totalCart = 0;

        while (true) {
            int batch = productWeightLogService.persistCartPendingFromHash(MAX_PER_RUN);
            if (batch == 0) break;

            totalCart += batch;
        }

        //hot 데이터 DB에 등록
        memberTagWeightHotService.mergeHotToMemberTagWeights();

        log.info("행동 등록 완료: 총 {}건", totalBehavior + totalCart);
    }

    //1분 후 bookmark 등록
    @Scheduled(fixedDelay = 60000)
    public void bookmarkPendingFlush() {
        productWeightLogService.confirmBookmarkPending();
    }
}
