package com.dto.project.domain.metatags.scheduler;

import com.dto.project.domain.metatags.service.ProductMetaTagAutoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductMetaTagAutoScheduler {

    private final ProductMetaTagAutoService productMetaTagAutoService;

    // 매일 새벽 3시 - 가중치 배치(2시) 이후 자동 메타태그 동기화
    @Scheduled(cron = "0 0 3 * * *")
    public void syncAllAutoTags() {
        log.info("[MetaTag Auto Scheduler] 자동 메타태그 동기화를 시작합니다.");
        productMetaTagAutoService.syncAllAutoTags();
        log.info("[MetaTag Auto Scheduler] 자동 메타태그 동기화를 완료했습니다.");
    }
}
