package com.ouweihao.community.service.Impl;

import com.ouweihao.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DateCountService implements com.ouweihao.community.service.DateCountService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    @Override
    public void recordUV(String ip) {
        String uvKey = RedisKeyUtil.getUVKey(dateFormat.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(uvKey, ip);
    }

    @Override
    public long calculateUV(Date startDate, Date endDate) {

        // 判空操作
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("日期设置不能为空！！");
        }

        // 键集
        List<String> keyLists = new ArrayList<>();

        // 整理该日期范围内的key
        Calendar startCalender = Calendar.getInstance();
        // 将开始日期传给startCalender
        startCalender.setTime(startDate);

        while (!startCalender.getTime().after(endDate)) {

            String key = RedisKeyUtil.getUVKey(dateFormat.format(startCalender.getTime()));

            keyLists.add(key);

            // 将startCalendar加1天
            startCalender.add(Calendar.DATE, 1);
        }

        String uvKey = RedisKeyUtil.getUVKey(dateFormat.format(startDate), dateFormat.format(endDate));

        redisTemplate.opsForHyperLogLog().union(uvKey, keyLists.toArray());

        return redisTemplate.opsForHyperLogLog().size(uvKey);
    }

    @Override
    public void recordDAU(int userId) {
        String dauKey = RedisKeyUtil.getDAUKey(dateFormat.format(new Date()));
        redisTemplate.opsForValue().setBit(dauKey, userId, true);
    }

    @Override
    public long calculateDAU(Date startDate, Date endDate) {

        // 判空操作
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("日期设置不能为空！！");
        }

        // 键集
        List<byte[]> keyLists = new ArrayList<>();

        // 整理该日期范围内的key
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);

        while (!startCalendar.getTime().after(endDate)) {

            // 得到键
            String key = RedisKeyUtil.getDAUKey(dateFormat.format(startCalendar.getTime()));
            keyLists.add(key.getBytes());

            // 日期加1
            startCalendar.add(Calendar.DATE, 1);
        }

        return ((long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String dauKey = RedisKeyUtil.getDAUKey(dateFormat.format(startDate), dateFormat.format(endDate));
                connection.bitOp(RedisStringCommands.BitOperation.OR, dauKey.getBytes(), keyLists.toArray(new byte[0][0]));
                return connection.bitCount(dauKey.getBytes());
            }
        }));

    }
}
