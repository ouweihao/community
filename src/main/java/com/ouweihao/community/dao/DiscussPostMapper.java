package com.ouweihao.community.dao;

import com.ouweihao.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    // 当UserId为0时则不拼接SQL，主要是考虑到后面用户个人中心会有我发布的帖子的功能
    // offset页数     limit每页最多显示多少数据

    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode, int sectionId);

    List<DiscussPost> selectDiscussPostBySectionId(int sectionId, int offset, int limit, int orderMode);

    // userId用处同上
    // @Param注解用于给参数取别名
    // 如果只有一个参数，并且在<if>（动态SQL）里使用，则必须取别名

    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost post);

    DiscussPost selectDiscussPostById(int id);

    int selectSectionDiscussPostCount(int sectionId);

    List<Integer> selectSectionDiscussPostId(int sectionId);

    int updateCommentCount(int id, int commentCount);

    int updateType(int postId, int type);

    int updateStatus(int postId, int status);

    int updateScore(int postId, double score);

    int updateViews(int postId, int views);

    int updateAttach(int postId, String attachName, String attachUrl);

    int deleteAttach(int postId);

    int updatePost(DiscussPost post);

    int deletePostBySectionId(int sectionId);

}
