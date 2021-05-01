package com.ouweihao.community.controller;

import com.ouweihao.community.entity.Page;
import com.ouweihao.community.entity.User;
import com.ouweihao.community.service.FollowService;
import com.ouweihao.community.service.UserService;
import com.ouweihao.community.util.CommunityConstant;
import com.ouweihao.community.util.CommunityUtil;
import com.ouweihao.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.follow(user.getId(), entityType, entityId);

        return CommunityUtil.getJsonString(0, "关注成功！！");
    }

    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.unfollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJsonString(0, "取消关注成功！！");
    }

    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {

        User target = userService.findUserById(userId);

        if (target == null) {
            throw new RuntimeException("该用户不存在！！");
        }

        model.addAttribute("target", target);

        // 设置分页条件
        page.setPath("/followees/" + userId);
        page.setLimit(5);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_USER));

        // 得到关注的列表
        List<Map<String, Object>> followees = followService.findFollowees(userId, page.getOffSet(), page.getLimit());

        if (followees != null) {
            for (Map<String, Object> followee : followees) {
                User user = (User) followee.get("user");
                boolean hasFollowed = hasFollowed(user.getId());
                followee.put("hasFollowed", hasFollowed);
            }
        }

        model.addAttribute("users", followees);

        return "/site/followee";
    }

    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {

        // 得到所要查询的用户
        User target = userService.findUserById(userId);

        if (target == null) {
            throw new RuntimeException("该用户不存在！！");
        }

        model.addAttribute("target", target);

        page.setRows((int) followService.findFollowerCount(ENTITY_USER, userId));
        page.setLimit(5);
        page.setPath("/followers/" + userId);

        List<Map<String, Object>> followers = followService.findFollowers(userId, page.getOffSet(), page.getLimit());

        if (followers != null) {
            for (Map<String, Object> follower : followers) {
                User user = (User) follower.get("user");
                follower.put("hasFollowed", hasFollowed(user.getId()));
            }
        }
        model.addAttribute("users", followers);

        return "/site/follower";
    }

    private boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null) {
            return false;
        }

        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_USER, userId);
    }


}
