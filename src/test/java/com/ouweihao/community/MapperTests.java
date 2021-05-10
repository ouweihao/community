package com.ouweihao.community;

import com.ouweihao.community.dao.DiscussPostMapper;
import com.ouweihao.community.dao.LoginTicketMapper;
import com.ouweihao.community.dao.MessageMapper;
import com.ouweihao.community.dao.UserMapper;
import com.ouweihao.community.entity.DiscussPost;
import com.ouweihao.community.entity.LoginTicket;
import com.ouweihao.community.entity.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectPosts() {
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(149, 0, 10, 0);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }

        int postRows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(postRows);
    }

    @Test
    public void testInsertLoginTicket() {
        LoginTicket ticket = new LoginTicket();
        ticket.setId(1);
        ticket.setUserId(123);
        ticket.setTicket("abc");
        ticket.setStatus(0);
        ticket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));

        loginTicketMapper.insertLoginTicket(ticket);
    }

    @Test
    public void testSelectAndUpdateLoginTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectLoginTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc", 1);

        loginTicket = loginTicketMapper.selectLoginTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void testMessageMapper() {
        List<Message> messageList = messageMapper.selectConversations(111, 0, 20);
        for (Message message : messageList) {
            System.out.println(message);
        }

        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        messageList = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : messageList) {
            System.out.println(message);
        }

        count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count = messageMapper.selectUnreadLetterCount(131, "111_131");
        System.out.println(count);

    }

}
