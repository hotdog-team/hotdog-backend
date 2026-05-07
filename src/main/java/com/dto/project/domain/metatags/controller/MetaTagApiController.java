package com.dto.project.domain.metatags.controller;

import com.dto.project.domain.metatags.dto.MetaTagDTO;
import com.dto.project.domain.metatags.service.MetaTagService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tags")
public class MetaTagApiController {
    private final MetaTagService metaTagService;

    public MetaTagApiController(MetaTagService metaTagService){
        this.metaTagService = metaTagService;
    }

    @GetMapping
    public List<MetaTagDTO> listAllMetaTags() {
        return metaTagService.listAllMetaTag();
    }

    @GetMapping("/{id}")
    public MetaTagDTO getTagDetail(@PathVariable("id") int id){
        return metaTagService.detailViewMetaTag(id);
    }

}
