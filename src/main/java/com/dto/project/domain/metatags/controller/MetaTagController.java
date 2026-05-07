package com.dto.project.domain.metatags.controller;

import com.dto.project.domain.metatags.dto.MetaTagDTO;
import com.dto.project.domain.metatags.service.MetaTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class MetaTagController {
    @Autowired
    MetaTagService service;

    //메타 태그 조회
    @GetMapping("/admin/tags/")
    public String listAllMetaTags(Model model){
        List<MetaTagDTO> mtgList = service.listAllMetaTag();
        model.addAttribute("mtgList", mtgList);
        return "admin/tags/tagListView";
    }

    //메타 태그 등록 폼
    @GetMapping("/admin/tags/newTagForm")
    public String viewNewTagForm(){
        return "admin/tags/newTagForm";
    }

    @PostMapping("/admin/tags/insertTag")
    public String insertTag(MetaTagDTO mtgVo){
        service.insertMetaTag(mtgVo);

        return "redirect:/admin/tags";
    }

    @GetMapping("/admin/tags/{id}")
    public String viewDetailTag(@PathVariable int id, Model model){
        MetaTagDTO mtgVo = service.detailViewMetaTag(id);
        model.addAttribute("mtgVo", mtgVo);
        return "admin/tags/viewDetailTag";
    }

    @GetMapping("/admin/tags/updateTagForm/{id}")
    public String viewUpdateTagForm(@PathVariable int id, Model model){
        MetaTagDTO mtgVo = service.detailViewMetaTag(id);
        model.addAttribute("mtgVo", mtgVo);
        return "admin/tags/updateTagForm";
    }

    @PostMapping("/admin/tags/updateTag")
    public String updateTag(MetaTagDTO mtgVo){
        service.updateMetaTag(mtgVo);

        return "redirect:/admin/tags";
    }

    @RequestMapping("/admin/tags/deleteTag/{id}")
    public String deleteTag(@PathVariable int id){
        service.deleteMetaTag(id);

        return "redirect:/admin/tags";
    }
}
