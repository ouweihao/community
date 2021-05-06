package com.ouweihao.community;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class BlockingQueueTests {

    public static void main(String[] args) {

        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10);
        new Thread(new producer(queue)).start();
        new Thread(new consumer(queue)).start();
        new Thread(new consumer(queue)).start();
        new Thread(new consumer(queue)).start();

    }

}

class producer implements Runnable {

    private BlockingQueue<Integer> queue;

    public producer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(20);
                queue.put(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "生产:" + queue.size());
        }
    }
}

class consumer implements Runnable {

    private BlockingQueue<Integer> queue;

    public consumer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(new Random().nextInt(1000));
                queue.take();
                System.out.println(Thread.currentThread().getName() + "消费:" + queue.size());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
