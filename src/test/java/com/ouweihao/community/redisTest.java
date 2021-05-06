package com.ouweihao.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class redisTest {

    @Autowired
    private RedisTemplate<String, Object> template;

    @Test
    public void testStrings() {
        String redisKey = "test:count";

        template.opsForValue().set(redisKey, 1);

        System.out.println(template.opsForValue().get(redisKey));
        System.out.println(template.opsForValue().increment(redisKey));
        System.out.println(template.opsForValue().decrement(redisKey));

    }

    @Test
    public void testHashes() {
        String redisKey = "test:user";

        template.opsForHash().put(redisKey, "id", 1);
        template.opsForHash().put(redisKey, "username", "zhangsan");

        System.out.println(template.opsForHash().get(redisKey, "id"));
        System.out.println(template.opsForHash().get(redisKey, "username"));
    }

    @Test
    public void testList() {
        String redisKey = "test:ids";

        template.opsForList().leftPush(redisKey, 200);
        template.opsForList().leftPush(redisKey, 201);
        template.opsForList().leftPush(redisKey, 202);

        System.out.println(template.opsForList().size(redisKey));
        System.out.println(template.opsForList().index(redisKey, 0));
        System.out.println(template.opsForList().range(redisKey, 0, 4));

        System.out.println(template.opsForList().leftPop(redisKey));
        System.out.println(template.opsForList().rightPop(redisKey));
        System.out.println(template.opsForList().leftPop(redisKey));
    }

    @Test
    public void testSet() {
        String redisKey = "test:teachers";

        template.opsForSet().add(redisKey, "张三", "李四", "王五");

        System.out.println(template.opsForSet().size(redisKey));
        System.out.println(template.opsForSet().pop(redisKey));
        System.out.println(template.opsForSet().members(redisKey));

    }

    @Test
    public void testSortedSet() {
        String redisKey = "test:student";

        template.opsForZSet().add(redisKey, "aaa", 10);
        template.opsForZSet().add(redisKey, "bbb", 15);
        template.opsForZSet().add(redisKey, "ccc", 20);

        System.out.println(template.opsForZSet().zCard(redisKey));
        System.out.println(template.opsForZSet().score(redisKey, "aaa"));
        System.out.println(template.opsForZSet().rank(redisKey, "aaa"));
        System.out.println(template.opsForZSet().reverseRank(redisKey, "aaa"));
        System.out.println(template.opsForZSet().range(redisKey, 0, 3));
        System.out.println(template.opsForZSet().reverseRange(redisKey, 0, 3));

    }

    @Test
    public void testKeys() {
        template.delete("test:user");

        System.out.println(template.hasKey("test:user"));

        template.expire("test:student", 10, TimeUnit.SECONDS);
    }

    @Test
    public void testBoundOperations() {
        String redisKey = "test:teachers";

        BoundSetOperations<String, Object> operations = template.boundSetOps(redisKey);

        operations.add("李四");
        operations.add("赵六");
        System.out.println(operations.members());
        System.out.println(operations.pop());
        System.out.println(operations.members());

    }

    // 编程式事务

    @Test
    public void testTranscational() {
        Object obj = template.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                String redisKey = "test:tx";

                // 开启事务
                operations.multi();

                operations.opsForSet().add(redisKey, "zhangsan");
                operations.opsForSet().add(redisKey, "lisi");
                operations.opsForSet().add(redisKey, "wangwu");

                System.out.println(operations.opsForSet().members(redisKey));

                // exec()提交事务
                return operations.exec();
            }
        });

        System.out.println(obj);
    }

}
