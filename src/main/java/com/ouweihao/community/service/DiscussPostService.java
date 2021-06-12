package com.ouweihao.community.service;

import com.ouweihao.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Param;

import java.util.List;

//@Service
public interface DiscussPostService {

    List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode, int sectionId);

    int findDiscussPostRows(@Param("userId") int userId);

    int addDiscussPost(DiscussPost discussPost);

    DiscussPost findDiscussPostById(int id);

    // 查询某分区下帖子的数量，若sectionId为0则为全部数量

    int findSectionDiscussPostCount(int sectionId);

    int updateCommentCount(int id, int count);

    int updateType(int postId, int type);

    int updateStatus(int postId, int status);

    int updateScore(int postId, double score);

    int updateViews(int postId, int views);

    int updateAttach(int postId, String attachName, String attachUrl);

    int deleteAttach(int postId);

    int updatePost(DiscussPost post);

    int deletePostBySectionId(int sectionId);

    List<Integer> findSectionDiscussPostId(int sectionId);

}
