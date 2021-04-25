package com.ouweihao.community.service;

import com.ouweihao.community.dao.DiscussPostMapper;
import com.ouweihao.community.dao.UserMapper;
import com.ouweihao.community.entity.DiscussPost;
import com.ouweihao.community.entity.User;
import com.ouweihao.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

//@Service
public class AlphaService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public AlphaService() {
        System.out.println("实例化AlphaService");
    }

    @PostConstruct
    public void init() {
        System.out.println("初始化AlphaService");
    }

    @PreDestroy
    public void destory() {
        System.out.println("销毁AlphaService");
    }


    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1() {
        User user = new User();
        user.setUsername("zhangsan");
        user.setPassword("123");
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setEmail("test@qq.com");
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl("http://images.nowcoder.com/head/310t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());  // insert之后，自动生成的id就会被自动加上
        post.setTitle("test");
        post.setContent("hello");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        Integer.parseInt("abc");

        return "ok";
    }

    public Object save2() {
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus transactionStatus) {
                User user = new User();
                user.setUsername("zhangsan2");
                user.setPassword("123");
                user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
                user.setEmail("test@qq.com");
                user.setType(0);
                user.setStatus(0);
                user.setActivationCode(CommunityUtil.generateUUID());
                user.setHeaderUrl("http://images.nowcoder.com/head/310t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());  // insert之后，自动生成的id就会被自动加上
                post.setTitle("test2");
                post.setContent("hello2");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);

                Integer.parseInt("abc");

                return "ok";
            }
        });

    }

}
