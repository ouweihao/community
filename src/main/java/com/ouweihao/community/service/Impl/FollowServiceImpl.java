package com.ouweihao.community.service.Impl;

import com.ouweihao.community.entity.User;
import com.ouweihao.community.service.FollowService;
import com.ouweihao.community.service.UserService;
import com.ouweihao.community.util.CommunityConstant;
import com.ouweihao.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowServiceImpl implements FollowService, CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Override
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {

                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                redisTemplate.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                redisTemplate.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return operations.exec();
            }
        });
    }

    @Override
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {

                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                redisTemplate.opsForZSet().remove(followeeKey, entityId);
                redisTemplate.opsForZSet().remove(followerKey, userId);

                return operations.exec();
            }
        });
    }

    @Override
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    @Override
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    @Override
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    @Override
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {

        // 得到键的名称
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_USER);

        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);

        if (targetIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();

        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();

            User user = userService.findUserById(targetId);
            map.put("user", user);

            long followTimeStamp = redisTemplate.opsForZSet().score(followeeKey, targetId).longValue();
            map.put("followTime", new Date(followTimeStamp));

            list.add(map);
        }

        return list;
    }

    @Override
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {

        // 得到键
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_USER, userId);

        // 所有粉丝id所组成的集合
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);

        if (targetIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();

        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();

            User user = userService.findUserById(targetId);
            map.put("user", user);

            long followTimeStamp = redisTemplate.opsForZSet().score(followerKey, targetId).longValue();
            map.put("followTime", new Date(followTimeStamp));

            list.add(map);
        }

        return list;
    }


}
