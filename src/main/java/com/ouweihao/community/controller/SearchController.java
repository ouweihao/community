package com.ouweihao.community.controller;

import com.ouweihao.community.entity.DiscussPost;
import com.ouweihao.community.entity.Page;
import com.ouweihao.community.service.ElasticSearchService;
import com.ouweihao.community.service.LikeService;
import com.ouweihao.community.service.SectionService;
import com.ouweihao.community.service.UserService;
import com.ouweihao.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticSearchService searchService;

    @Autowired
    private LikeService likeService;

    /**
     * 搜到帖子后还要显示作者的信息
     */
    @Autowired
    private UserService userService;

    /**
     * 用于查询帖子分区信息
     */
    @Autowired
    private SectionService sectionService;

    // 路径  search?keyword=XXX

    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) {

        // 搜索帖子，帖子页数，因为我们封装的page的页数是从第1页开始的，而Spring提供的page是从第0页开始的，所以我们的页数要减1
        org.springframework.data.domain.Page<DiscussPost> searchResults
                = searchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());

        List<Map<String, Object>> posts = new ArrayList<>();

        if (searchResults != null) {
            for (DiscussPost post : searchResults) {
                Map<String, Object> map = new HashMap<>();

                // 存入帖子
                map.put("post", post);

                map.put("section", sectionService.findSectionById(post.getSectionId()));

                System.out.println("views: " + post.getViews());

                // 存入作者
                map.put("author", userService.findUserById(post.getUserId()));

                // 存入点赞数
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_POST, post.getId()));

                posts.add(map);
            }
        }

        model.addAttribute("posts", posts);
        // 向模板中添加关键词，以便在页面上显示
        model.addAttribute("keyword", keyword);

        // 存入分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResults == null ? 0 : (int) searchResults.getTotalElements());

        return "/site/search";
    }

}
