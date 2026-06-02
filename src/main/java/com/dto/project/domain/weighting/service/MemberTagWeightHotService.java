package com.dto.project.domain.weighting.service;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.member.repository.MemberRepository;
import com.dto.project.domain.metatags.entity.MetaTagProduct;
import com.dto.project.domain.metatags.repository.MetaTagProductRepository;
import com.dto.project.domain.metatags.repository.MetaTagRepository;
import com.dto.project.domain.weighting.dto.MemberTagHotScore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

//가중치 점수를 바로 Redis로 적용한다
@Service
@RequiredArgsConstructor
public class MemberTagWeightHotService {

    private static final String HOT_KEY_PREFIX = "weight:hot:";

    private final MetaTagProductRepository metaTagProductRepository;
    private final MetaTagRepository metaTagRepository;
    private final MemberRepository memberRepository;
    private final MemberTagWeightService memberTagWeightService;
    private final StringRedisTemplate redisTemplate;

    public void increaseFromProduct(Long memberId, Long productId, int delta) {
        applyDeltaFromProduct(memberId, productId, delta);
    }

    public void decreaseFromProduct(Long memberId, Long productId, int delta) {
        if (!redisTemplate.hasKey(hotKey(memberId))) return;
        applyDeltaFromProduct(memberId, productId, -delta);
    }

    //조회시 사용(아직 비워둠)
    public List<MemberTagHotScore> getAllScore(Long memberId) {
        return null;
    }

    // 배치 마감: hot hash 전체를 member_tag_weights에 반영 후 hot 삭제
    public void mergeHotToMemberTagWeights() {
        Set<String> keys = redisTemplate.keys(HOT_KEY_PREFIX + "*");
        if (keys.isEmpty()) return;

        int mergedMemberCount = 0;
        for (String key : keys) {
            Long memberId = Long.parseLong(key.substring(HOT_KEY_PREFIX.length()));
            Member member = memberRepository.findById(memberId).orElse(null);
            if (member == null) {
                redisTemplate.delete(key);
                continue;
            }

            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                Long metaTagId = Long.parseLong((String) entry.getKey());
                int delta = Integer.parseInt((String) entry.getValue());
                if (delta == 0) {
                    continue;
                }
                metaTagRepository.findById(metaTagId)
                        .ifPresent(tag -> memberTagWeightService.applyBehaviorScore(member, tag, delta));
            }
            redisTemplate.delete(key);
            mergedMemberCount++;
        }
    }

    //apply로 메서드를 분리처리한다
    private void applyDeltaFromProduct(Long memberId, Long productId, int delta) {
        if (delta == 0) return;
        List<MetaTagProduct> mappings = metaTagProductRepository.findByProduct_Id(productId);
        if (mappings.isEmpty()) return;
        String key = hotKey(memberId);
        for (MetaTagProduct mapping : mappings) {
            //hash 형태로 저장한다
            redisTemplate.opsForHash().increment(key, String.valueOf(mapping.getMetaTag().getId()), delta);
        }
    }

    private static String hotKey(Long memberId) {
        return HOT_KEY_PREFIX + memberId;
    }
}

