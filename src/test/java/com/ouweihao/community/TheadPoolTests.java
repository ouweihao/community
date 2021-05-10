package com.ouweihao.community;

import com.ouweihao.community.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class TheadPoolTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(TheadPoolTests.class);

    // JDK普通线程池，创建了有5个线程的线程池
    ExecutorService executorService = Executors.newFixedThreadPool(5);

    // 创建了一个具有5个线程的可执行定时任务的线程池
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    // Spring普通线程池
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    // Spring可执行定时任务线程池
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private AlphaService alphaService;

    private void sleep(long m) {
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 1.JDK普通线程池
    @Test
    public void testExecutorService() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                LOGGER.debug("hello, executorservice");
            }
        };

        for (int i = 0; i < 10; i++) {
            executorService.submit(runnable);
        }

        sleep(10000);

    }

    // 2. JDK可执行定时任务的线程池
    @Test
    public void testScheduleExecutorService() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                LOGGER.debug("hello, scheduleExecutorService");
            }
        };

        scheduledExecutorService.scheduleAtFixedRate(task, 10000, 1000, TimeUnit.MILLISECONDS);

        sleep(30000);
    }

    // 3. Spring普通线程池执行任务
    @Test
    public void testThreadPoolTaskExecutor() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                LOGGER.debug("hello, ThreadPoolTaskExecutor");
            }
        };

        for (int i = 0; i < 10; i++) {
            taskExecutor.submit(task);
        }

        sleep(10000);
    }

    // 4. Spring可定时执行任务线程池
    @Test
    public void testThreadPoolScheduleExecutor() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                LOGGER.debug("hello, ThreadPoolScheduleExecutor");
            }
        };

        Date startDate = new Date(System.currentTimeMillis() + 10000);
        taskScheduler.scheduleAtFixedRate(task, startDate, 1000);

        sleep(30000);
    }

    // 5. Spring普通线程池简化
    @Test
    public void testThreadPoolTaskExecutorSimplified() {
        for (int i = 0; i < 10; i++) {
            alphaService.Execute1();
        }

        sleep(10000);
    }

    // 6. Spring可执行定时任务线程池简化
    @Test
    public void testThreadPoolTaskScheduleSimplified() {
//        alphaService.Excute2();

        sleep(30000);
    }

}
