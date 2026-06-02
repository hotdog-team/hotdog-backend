package com.dto.project.domain.weighting.dto;

import com.dto.project.domain.metatags.entity.MetaTagType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MemberTagHotScore {
    private Long metaTagId;
    private MetaTagType type;
    private int dbScore;
    private int hotDelta;
    private int weightScore;
    //최종 적용 점수
    private double finalScore;
}
