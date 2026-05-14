package com.dto.project.domain.metatags.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MetaTagProductReplaceRequest {

    @NotNull(message = "metaTagIds는 필수 입력값입니다.")
    private List<Long> metaTagIds;
}
