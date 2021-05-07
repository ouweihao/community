package com.ouweihao.community.service;

import com.ouweihao.community.entity.DiscussPost;
import org.springframework.data.domain.Page;

public interface ElasticSearchService {

    void saveDiscussPost(DiscussPost discussPost);

    void deleteDiscussPost(int id);

    /**
     * 根据关键词搜索帖子
     *
     * @param keyword 关键词
     * @param current 当前在第几页
     * @param limit   每页显示的帖子数量
     * @return 由帖子构成的一个page对象
     */
    Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit);

}
