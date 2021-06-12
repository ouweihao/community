package com.ouweihao.community.controller;

import com.ouweihao.community.entity.Page;
import com.ouweihao.community.entity.Section;
import com.ouweihao.community.service.DiscussPostService;
import com.ouweihao.community.service.ElasticSearchService;
import com.ouweihao.community.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/section")
public class SectionController {

    @Autowired
    private SectionService sectionService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public String getSeciontPage(Page page, Model model) {

        // 设置页面信息
        page.setLimit(10);
        page.setPath("/section/list");
        page.setRows(sectionService.findSectionCount());

        List<Section> sections = sectionService.findSections(page.getOffSet(), page.getLimit());

        model.addAttribute("sections", sections);

        return "/site/section-list";
    }

    @RequestMapping(path = "/add", method = RequestMethod.GET)
    public String getSectionAddPage(Model model) {
        Section section = new Section();

        model.addAttribute("section", section);

        return "/site/section-input";
    }

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public String insertSection(@RequestParam(name = "sectionName") String newName, RedirectAttributes attributes) {

        Section section = new Section();
        section.setName(newName);

        Map<String, String> message = sectionService.addSection(section);

        attributes.addFlashAttribute("message", message);

        return "redirect:/section/list";
    }

    @RequestMapping(path = "/update/{id}", method = RequestMethod.GET)
    public String getUpdatePage(@PathVariable(name = "id") int id, Model model) {

        Section section = sectionService.findSectionById(id);

        model.addAttribute("section", section);

        return "/site/section-input";
    }

    @RequestMapping(path = "/update/{id}", method = RequestMethod.POST)
    public String updateSection(@PathVariable(name = "id") int id,
                                @RequestParam(name = "sectionName") String newName, RedirectAttributes attributes) {

        Section section = sectionService.findSectionById(id);
        section.setName(newName);

        Map<String, String> message = sectionService.updateSection(section);

        attributes.addFlashAttribute("message", message);

        return "redirect:/section/list";
    }

    @RequestMapping(path = "/delete/{id}", method = RequestMethod.GET)
    public String deleteSection(@PathVariable(name = "id") int id, RedirectAttributes attributes) {

        sectionService.deleteSection(id);

        discussPostService.deletePostBySectionId(id);

        List<Integer> sectionDiscussPostId = discussPostService.findSectionDiscussPostId(id);

        for (Integer postId : sectionDiscussPostId) {
            elasticSearchService.deleteDiscussPost(postId);
        }

        Map<String, String> message = new HashMap<>();

        message.put("nameMsg", "删除版块成功！！！");

        attributes.addFlashAttribute("message", message);

        return "redirect:/section/list";
    }

}
