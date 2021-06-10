package com.ouweihao.community.controller;

import com.ouweihao.community.entity.DiscussPost;
import com.ouweihao.community.entity.Page;
import com.ouweihao.community.entity.Section;
import com.ouweihao.community.entity.User;
import com.ouweihao.community.service.DiscussPostService;
import com.ouweihao.community.service.LikeService;
import com.ouweihao.community.service.SectionService;
import com.ouweihao.community.service.UserService;
import com.ouweihao.community.util.CommunityConstant;
import com.ouweihao.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private SectionService sectionService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page,
                               @RequestParam(name = "orderMode", defaultValue = "0") int orderMode,
                               @RequestParam(name = "sectionId", defaultValue = "0") int sectionId) {
        // 方法调用前，SpringMVC会自动实例化Model和Page，并将Page注入Model。
        // 可以直接在页面中访问Page中的数据
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode=" + orderMode + "&sectionId=" + sectionId);

        List<DiscussPost> list =
                discussPostService.findDiscussPosts(0, page.getOffSet(), page.getLimit(), orderMode, sectionId);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost discussPost : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", discussPost);
                User user = userService.findUserById(discussPost.getUserId());
                map.put("user", user);

                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_POST, discussPost.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("loginUser", hostHolder.getUser());
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode", orderMode);
        model.addAttribute("sectionId", sectionId);

        List<Map<String, Object>> sections = new ArrayList<>();
        List<Section> allSections = sectionService.getAllSections();

        int allPostCount = 0;

        for (Section s : allSections) {
            Map<String, Object> section = new HashMap<>();
            section.put("section", s);
            int count = discussPostService.findSectionDiscussPostCount(s.getId());
            allPostCount += count;
            section.put("postCount", count);
            sections.add(section);
        }

        model.addAttribute("sections", sections);
        model.addAttribute("allPostCount", allPostCount);

        return "/index";
    }

    @RequestMapping(path = "/add/{id}", method = RequestMethod.GET)
    public String getModifyPage(@PathVariable(name = "id") int id, Model model) {
        DiscussPost post = discussPostService.findDiscussPostById(id);

        if (hostHolder.getUser() == null) {
            return "redirect:/index";
        }

        if (hostHolder.getUser().getId() != post.getUserId()) {
            return "redirect:/index";
        }

        model.addAttribute("post", post);

        List<Section> allSections = sectionService.getAllSections();
        model.addAttribute("sections", allSections);

        return "/site/discusspost_input";
    }

    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }

}
