package com.ouweihao.community.service.Impl;

import com.ouweihao.community.dao.DiscussPostMapper;
import com.ouweihao.community.entity.DiscussPost;
import com.ouweihao.community.service.DiscussPostService;
import com.ouweihao.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostServiceImpl implements DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Override
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    @Override
    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    @Override
    public int addDiscussPost(DiscussPost discussPost) {

        // 空值判断与处理
        if (discussPost == null) {
            throw new IllegalArgumentException("帖子不能为空！！");
        }

//        System.out.println(content);

        // 标签预处理
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        // 过滤敏感字符
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        // 只把内容改成html的格式
        discussPost.setContent(HtmlUtils.htmlUnescape(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    @Override
    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    @Override
    public int updateCommentCount(int id, int count) {
        return discussPostMapper.updateCommentCount(id, count);
    }

    @Override
    public int updateType(int postId, int type) {
        return discussPostMapper.updateType(postId, type);
    }

    @Override
    public int updateStatus(int postId, int status) {
        return discussPostMapper.updateStatus(postId, status);
    }

    @Override
    public int updateScore(int postId, double score) {
        return discussPostMapper.updateScore(postId, score);
    }

    @Override
    public int updateViews(int postId, int views) {
        return discussPostMapper.updateViews(postId, views);
    }
}
