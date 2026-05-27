package com.dto.project.domain.weighting.scheduler;

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

    private final ProductWeightLogService productWeightLogService;

    //매일 새벽 2시에 일괄 등록(batch)하도록 처리(팀내 policy에 의거)
    @Scheduled(cron = "0 0 2 * * *")
    public void behaviorFlush() {
        int totalCase = 0;

        while(true) {
            int queue = productWeightLogService.persistBehaviorLogsFromQueue(MAX_PER_RUN);
            //queue가 비면 종료
            if (queue == 0) break;

            totalCase += queue;
        }

        log.info("등록 완료: 총 {}건", totalCase);
    }

    //1분 후 bookmark 등록
    @Scheduled(fixedDelay = 60000)
    public void bookmarkPendingFlush() {
        productWeightLogService.confirmBookmarkPending();
    }
}
