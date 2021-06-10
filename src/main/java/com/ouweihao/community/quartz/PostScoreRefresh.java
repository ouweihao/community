package com.ouweihao.community.quartz;

import com.ouweihao.community.entity.DiscussPost;
import com.ouweihao.community.service.DiscussPostService;
import com.ouweihao.community.service.ElasticSearchService;
import com.ouweihao.community.service.LikeService;
import com.ouweihao.community.util.CommunityConstant;
import com.ouweihao.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefresh implements Job, CommunityConstant {

    // 用于记录日志

    private static final Logger LOGGER = LoggerFactory.getLogger(PostScoreRefresh.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    // 社区创建时间
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2021-01-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化社区时间失败！！");
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String postScoreKey = RedisKeyUtil.getPostScoreKey();

        BoundSetOperations operations = redisTemplate.boundSetOps(postScoreKey);

        if (operations.size() == 0) {
            LOGGER.info("[任务取消] 没有需要刷新分数的帖子");
            return;
        }

        LOGGER.info("[任务开始] 正在刷新帖子分数： " + operations.size());

        while (operations.size() > 0) {
            Integer postId = (Integer) operations.pop();
            this.refreshScore(postId);
        }

        LOGGER.info("[任务结束] 刷新帖子分数完成！！");

    }

    private void refreshScore(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);

        if (post == null) {
            LOGGER.error("该帖子不存在，帖子Id为：" + postId);
            return;
        }

        // 是否加精
        boolean wonderful = post.getStatus() == 1;

        // 评论数
        int commentCount = post.getCommentCount();

        // 点赞数
        long likeCount = likeService.findEntityLikeCount(ENTITY_POST, postId);

        // 浏览量
        int views = post.getViews();

        // 计算权重
        double weight = (wonderful ? 75 : 0) + commentCount * 10L + likeCount * 2 + views;

        // 分数
        double score = Math.log10(Math.max(weight, 1))
                + (post.getUpdateTime().getTime() - epoch.getTime() -
                (post.getUpdateTime().getTime() - post.getCreateTime().getTime()) * 0.1) / (1000 * 3600 * 24 * 1.0);

        // 更新帖子分数
        discussPostService.updateScore(postId, score);

        // 同步搜索数据
        post.setScore(score);
        elasticSearchService.saveDiscussPost(post);
    }
}
