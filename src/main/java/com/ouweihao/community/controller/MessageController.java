package com.ouweihao.community.controller;

import com.ouweihao.community.entity.Message;
import com.ouweihao.community.entity.Page;
import com.ouweihao.community.entity.User;
import com.ouweihao.community.service.MessageService;
import com.ouweihao.community.service.UserService;
import com.ouweihao.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    // 得到当前用户

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetter(Page page, Model model) {
        // 得到当前用户
        User user = hostHolder.getUser();
        int userId = user.getId();

        // 设置页面信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(userId));

        // 得到会话
        List<Message> messgeList = messageService.findConversations(userId, page.getOffSet(), page.getLimit());

        List<Map<String, Object>> conversations = new ArrayList<>();

        if (messgeList != null) {
            for (Message message : messgeList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                map.put("unreadLetterCount", messageService.findUnreadLetterCount(userId, message.getConversationId()));
                int targetId = userId == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);
            }
        }

        model.addAttribute("conversations", conversations);
        model.addAttribute("allUnreadLetterCount", messageService.findUnreadLetterCount(userId, null));

        return "/site/letter.html";

    }

    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        List<Message> letterList = messageService.findLetters(conversationId, page.getOffSet(), page.getLimit());

        List<Map<String, Object>> letters = new ArrayList<>();

        if (letterList != null) {
            for (Message letter : letterList) {
                Map<String, Object> map = new HashMap<>();

                map.put("letter", letter);

                User user = userService.findUserById(letter.getFromId());
                map.put("fromUser", user);

                letters.add(map);
            }
        }

        model.addAttribute("letters", letters);

        // 私信目标
        User targetUser = getTargetUser(conversationId);
        model.addAttribute("targetUser", targetUser);

        return "/site/letter-detail";
    }

    private User getTargetUser(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }

    }

}
