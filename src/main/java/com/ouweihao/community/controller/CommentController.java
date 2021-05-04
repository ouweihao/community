package com.ouweihao.community.controller;

import com.ouweihao.community.annotation.LoginRequired;
import com.ouweihao.community.entity.Comment;
import com.ouweihao.community.entity.DiscussPost;
import com.ouweihao.community.entity.Event;
import com.ouweihao.community.event.EventProducer;
import com.ouweihao.community.service.CommentService;
import com.ouweihao.community.service.DiscussPostService;
import com.ouweihao.community.util.CommunityConstant;
import com.ouweihao.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping(path = "/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private EventProducer eventProducer;

    @LoginRequired
    @RequestMapping(path = "/add/{postId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("postId") int postId, Comment comment) {
        // 添加评论所未填写的信息
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());

        commentService.addComment(comment);

        // 触发消息事件

        Event event = new Event();
        event.setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", postId);

        // 设置发送的对象，即对应实体的作者
        if (comment.getEntityType() == ENTITY_POST) {
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setAuthorId(target.getUserId());
        } else {
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setAuthorId(target.getUserId());
        }

        eventProducer.fireEvent(event);

        return "redirect:/discuss/detail/" + postId;
    }

}
