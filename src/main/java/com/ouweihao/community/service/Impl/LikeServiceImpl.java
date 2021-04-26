package com.ouweihao.community.service.Impl;

import com.ouweihao.community.service.LikeService;
import com.ouweihao.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Override
    public void like(int userId, int entityType, int entityId) {
        // 得到键
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        // 得到用户是否给此实体点过赞
        Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);

        // 若点过赞则取消赞，若没点赞则进行点赞操作
        if (isMember) {
            redisTemplate.opsForSet().remove(entityLikeKey, userId);
        } else {
            redisTemplate.opsForSet().add(entityLikeKey, userId);
        }
    }

    @Override
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);

        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    @Override
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }
}
