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
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode, int sectionId) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode, sectionId);
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
        discussPost.setHtmlcontent(HtmlUtils.htmlEscape(discussPost.getHtmlcontent()));

        // 过滤敏感字符
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setHtmlcontent(sensitiveFilter.filter(discussPost.getHtmlcontent()));

        // 只把内容改成html的格式
        discussPost.setHtmlcontent(HtmlUtils.htmlUnescape(discussPost.getHtmlcontent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    @Override
    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    @Override
    public int findSectionDiscussPostCount(int sectionId) {
        return discussPostMapper.selectSectionDiscussPostCount(sectionId);
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

    @Override
    public int updateAttach(int postId, String attachName, String attachUrl) {
        return discussPostMapper.updateAttach(postId, attachName, attachUrl);
    }

    @Override
    public int deleteAttach(int postId) {
        return discussPostMapper.deleteAttach(postId);
    }

    @Override
    public int updatePost(DiscussPost post) {
        return discussPostMapper.updatePost(post);
    }

    @Override
    public int deletePostBySectionId(int sectionId) {
        return discussPostMapper.deletePostBySectionId(sectionId);
    }

    @Override
    public List<Integer> findSectionDiscussPostId(int sectionId) {
        return discussPostMapper.selectSectionDiscussPostId(sectionId);
    }
}
