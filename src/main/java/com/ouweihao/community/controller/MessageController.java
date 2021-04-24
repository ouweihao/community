package com.ouweihao.community.controller;

import com.ouweihao.community.entity.Message;
import com.ouweihao.community.entity.Page;
import com.ouweihao.community.entity.User;
import com.ouweihao.community.service.MessageService;
import com.ouweihao.community.service.UserService;
import com.ouweihao.community.util.CommunityUtil;
import com.ouweihao.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

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

        // 设置已读
        List<Integer> ids = getUnreadLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    private List<Integer> getUnreadLetterIds(List<Message> letterList) {
        ArrayList<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    // 当前用户是信息的接受者，且改信息状态为未读
                    ids.add(message.getId());
                }
            }
        }

        return ids;
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

    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
        // 得到会话对象
        User target = userService.findUserByName(toName);
        User sender = hostHolder.getUser();

        if (target == null) {
            return CommunityUtil.getJsonString(1, "对话对象不存在，请确认后再输入！！");
        }

        // 不能自己给自己发送私信
        if (target.getId() == sender.getId()) {
            return CommunityUtil.getJsonString(1, "不能自己给自己发消息哦！亲~");
        }

        Message message = new Message();

        message.setFromId(sender.getId());
        message.setToId(target.getId());

        String conversationId = message.getFromId() > message.getToId() ? message.getToId() + "_" + message.getFromId() : message.getFromId() + "_" + message.getToId();
        message.setConversationId(conversationId);

        message.setContent(content);

        message.setCreateTime(new Date());

        // 刚发送的消息状态设为0
        message.setStatus(0);

        messageService.addMessage(message);

        // 若为插入成功，后面统一处理

        return CommunityUtil.getJsonString(0);
    }

}
