package com.ouweihao.community.controller;

import com.ouweihao.community.annotation.LoginRequired;
import com.ouweihao.community.entity.User;
import com.ouweihao.community.service.LikeService;
import com.ouweihao.community.util.CommunityUtil;
import com.ouweihao.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityAuthorId) {

        // 得到当前用户
        User currentUser = hostHolder.getUser();
        if (currentUser == null) {
            return CommunityUtil.getJsonString(1, "您还未登录！");
        }

        // 点赞行为
        likeService.like(currentUser.getId(), entityType, entityId, entityAuthorId);

        // 点赞总数
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 当前用户是否对此实体点过赞，找到他的点赞状态
        int likeStatus = likeService.findEntityLikeStatus(currentUser.getId(), entityType, entityId);

        // 向前端传输数据进行更新
        Map<String, Object> map = new HashMap<>();

        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        return CommunityUtil.getJsonString(0, "点赞成功!!", map);
    }

}
