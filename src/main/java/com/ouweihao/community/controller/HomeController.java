package com.ouweihao.community.controller;

import com.ouweihao.community.entity.DiscussPost;
import com.ouweihao.community.entity.Page;
import com.ouweihao.community.entity.User;
import com.ouweihao.community.service.DiscussPostService;
import com.ouweihao.community.service.LikeService;
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
public class HomeController implements CommunityConstant {

    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        // 方法调用前，SpringMVC会自动实例化Model和Page，并将Page注入Model。
        // 可以直接在页面中访问Page中的数据
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffSet(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(list != null){
            for (DiscussPost discussPost : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", discussPost);
                User user = userService.findUserById(discussPost.getUserId());
                map.put("user", user);

                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_POST, discussPost.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }

}
