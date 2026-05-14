package com.dto.project.domain.metatags.dto;

import com.dto.project.domain.metatags.entity.MetaTagStatus;
import com.dto.project.domain.metatags.entity.MetaTagType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
public class MetaTagRequest {
    private Long id;

    @NotBlank(message = "이름은 필수 입력값입니다.")
    private String name;

    @NotNull(message = "태그 유형은 필수 입력값입니다.")
    private MetaTagType type;
    private MetaTagStatus metaTagStatus;
}
