package com.ouweihao.community;

import com.ouweihao.community.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class TranscationTest {

    @Autowired
    private AlphaService alphaService;

    @Test
    public void testTranscation() {
        Object o = alphaService.save1();
        System.out.println(o);
    }

    @Test
    public void testTranscation2() {
        Object o = alphaService.save2();
        System.out.println(o);
    }

}
