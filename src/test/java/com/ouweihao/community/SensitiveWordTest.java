package com.ouweihao.community;

import com.ouweihao.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveWordTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testFilter() {
        String text = "这里可以开房，嫖娼，吸毒和开票，等着你来！！！";
        String str = sensitiveFilter.filter(text);
        System.out.println(str);

        text = "这里可以~开~房~，~嫖~娼~，~吸~毒~和~开~票~，等着你来！！！";
        str = sensitiveFilter.filter(text);
        System.out.println(str);
    }

}
