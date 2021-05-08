package com.ouweihao.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
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

    // 统计20万个重复数据的独立总数

    @Test
    public void testHyperLogLog() {
        String redisKey = "test:hll:01";

        for (int i = 0; i <= 100000; i++) {
            template.opsForHyperLogLog().add(redisKey, i);
        }

        for (int i = 0; i <= 100000; i++) {
            int r = (int) (Math.random() * 100000) + 1;
            template.opsForHyperLogLog().add(redisKey, r);
        }

        Long size = template.opsForHyperLogLog().size(redisKey);

        System.out.println(size);

    }

    // 将3组数据合并，再统计合并后的重复数据的独立总数

    @Test
    public void testHyperLogLogUnion() {
        String redisKey2 = "test:hll:02";

        for (int i = 1; i < 10001; i++) {
            template.opsForHyperLogLog().add(redisKey2, i);
        }

        String redisKey3 = "test:hll:03";

        for (int i = 5001; i < 15001; i++) {
            template.opsForHyperLogLog().add(redisKey3, i);
        }

        String redisKey4 = "test:hll:04";

        for (int i = 10001; i < 20001; i++) {
            template.opsForHyperLogLog().add(redisKey4, i);
        }

        String unionKey = "test:hll:union";
        template.opsForHyperLogLog().union(unionKey, redisKey2, redisKey3, redisKey4);

        Long size = template.opsForHyperLogLog().size(unionKey);
        System.out.println(size);
    }

    // 统计一组数据的布尔值

    @Test
    public void testBitMap() {
        String redisKey = "test:bm:01";

        // 记录

        template.opsForValue().setBit(redisKey, 1, true);
        template.opsForValue().setBit(redisKey, 3, true);
        template.opsForValue().setBit(redisKey, 5, true);

        // 得到值

        for (int i = 0; i < 6; i++) {
            System.out.println(template.opsForValue().getBit(redisKey, i));
        }

        // 统计数据
        Object obj = template.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(obj);

    }

    // 统计三组数据的布尔值，并对这三组数据做OR运算

    @Test
    public void testBitmapOperation() {

        String redisKey02 = "test:bm:02";

        template.opsForValue().setBit(redisKey02, 1, true);
        template.opsForValue().setBit(redisKey02, 2, true);
        template.opsForValue().setBit(redisKey02, 3, true);

        String redisKey03 = "test:bm:03";

        template.opsForValue().setBit(redisKey03, 2, true);
        template.opsForValue().setBit(redisKey03, 3, true);
        template.opsForValue().setBit(redisKey03, 4, true);

        String redisKey04 = "test:bm:04";
        template.opsForValue().setBit(redisKey04, 3, true);
        template.opsForValue().setBit(redisKey04, 4, true);
        template.opsForValue().setBit(redisKey04, 5, true);

        String redisKey = "test:bm:union";

        Object obj = template.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), redisKey02.getBytes(), redisKey03.getBytes(), redisKey04.getBytes());

                return redisConnection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(obj);

        for (int i = 0; i <= 6; i++) {
            System.out.println(template.opsForValue().getBit(redisKey, i));
        }

    }

}
