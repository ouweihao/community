package com.ouweihao.community.service;

import com.ouweihao.community.entity.DiscussPost;
import org.springframework.data.domain.Page;

public interface ElasticSearchService {

    void saveDiscussPost(DiscussPost discussPost);

    void deleteDiscussPost(int id);

    Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit);

}
