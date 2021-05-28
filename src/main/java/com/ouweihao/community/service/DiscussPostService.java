package com.ouweihao.community.service;

import com.ouweihao.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Param;

import java.util.List;

//@Service
public interface DiscussPostService {

    List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode);

    int findDiscussPostRows(@Param("userId") int userId);

    int addDiscussPost(DiscussPost discussPost);

    DiscussPost findDiscussPostById(int id);

    int updateCommentCount(int id, int count);

    int updateType(int postId, int type);

    int updateStatus(int postId, int status);

    int updateScore(int postId, double score);

    int updateViews(int postId, int views);

}
