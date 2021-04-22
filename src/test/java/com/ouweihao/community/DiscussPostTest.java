package com.ouweihao.community;

import com.ouweihao.community.dao.DiscussPostMapper;
import com.ouweihao.community.entity.DiscussPost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class DiscussPostTest {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testInsertDiscusspost() {
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(163);
        discussPost.setTitle("test");
        discussPost.setContent("test");
        discussPost.setType(0);
        discussPost.setStatus(0);
        discussPost.setCreateTime(new Date());
        discussPost.setCommentCount(13);
        discussPost.setScore(0);
        discussPostMapper.insertDiscussPost(discussPost);

    }

}
