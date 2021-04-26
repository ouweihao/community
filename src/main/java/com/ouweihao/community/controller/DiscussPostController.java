package com.ouweihao.community.controller;

import com.ouweihao.community.entity.Comment;
import com.ouweihao.community.entity.DiscussPost;
import com.ouweihao.community.entity.Page;
import com.ouweihao.community.entity.User;
import com.ouweihao.community.service.CommentService;
import com.ouweihao.community.service.DiscussPostService;
import com.ouweihao.community.service.LikeService;
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

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

//    @LoginRequired

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();

        // 若未登陆则无法发布帖子
        if (user == null) {
            return CommunityUtil.getJsonString(403, "您还未登陆哦！！");
        }

        DiscussPost post = new DiscussPost();

        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        // 后面将统一处理异常
        return CommunityUtil.getJsonString(0, "发布成功！");
    }

    // page接收整理分页条件
    // 只要是实体类型（JavaBean）在方法传入中做参数，springMVC都会将其传入到Model里面

    @RequestMapping(path = "/detail/{postId}", method = RequestMethod.GET)
    public String getDiscussPostDetail(@PathVariable("postId") int postId, Model model, Page page) {
        // 查询帖子
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        model.addAttribute("post", post);

        // 查询作者
        User author = userService.findUserById(post.getUserId());
        model.addAttribute("author", author);

        // 点赞数
        long likeCount = likeService.findEntityLikeCount(ENTITY_POST, postId);
        model.addAttribute("likeCount", likeCount);

        // 点赞状态，若未登陆则显示没点过赞
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_POST, postId);
        model.addAttribute("likeStatus", likeStatus);

        // 设置帖子评论的分页信息
        // 每页显示五条评论
        page.setLimit(5);
        page.setPath("/discuss/detail/" + postId);
        // 总共有多少条评论，从帖子表查，这样比从评论表查效率要高
        page.setRows(post.getCommentCount());

        model.addAttribute("page", page);

        // 评论：给帖子的评论
        // 回复：给评论的评论

        // 存储整个帖子的评论
        List<Comment> commentList = commentService.findCommentByEntity(ENTITY_POST, postId,
                page.getOffSet(), page.getLimit());

        // 用于存储整个帖子的评论和其作者的信息
        List<Map<String, Object>> CommentVoList = new ArrayList<>();

        // 遍历整个commentList从而找到所有的回复
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 初始化
                HashMap<String, Object> commentVo = new HashMap<>();

                // 存储评论信息
                commentVo.put("comment", comment);

                // 存储用户信息
                User user = userService.findUserById(comment.getUserId());
                commentVo.put("user", user);

                // 点赞数
                likeCount = likeService.findEntityLikeCount(ENTITY_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);

                // 点赞状态，若未登陆则显示没点过赞
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                // 得到回复的列表，不分页
                List<Comment> replyList = commentService.findCommentByEntity(
                        ENTITY_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);

                // 用于存储回复的信息
                List<Map<String, Object>> replyVoList = new ArrayList<>();

                if (replyList != null) {
                    for (Comment reply : replyList) {
                        // 初始化
                        HashMap<String, Object> replyVo = new HashMap<>();

                        // 存储回复信息
                        replyVo.put("reply", reply);

                        // 存储回复用户信息
                        User u = userService.findUserById(reply.getUserId());
                        replyVo.put("user", u);

                        // 点赞数
                        likeCount = likeService.findEntityLikeCount(ENTITY_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);

                        // 点赞状态，若未登陆则显示没点过赞
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        // 是否是对评论的回复
                        User targetUser = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", targetUser);

                        // 向VOList中添加数据
                        replyVoList.add(replyVo);
                    }
                }

                // 向VOList中添加数据
                commentVo.put("replys", replyVoList);

                // 回复数量
                int replyCount = commentService.findCommentCount(ENTITY_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                // 向VOList中添加数据
                CommentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", CommentVoList);

        return "/site/discuss-detail";
    }

}
