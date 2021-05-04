package com.ouweihao.community.service;

import com.ouweihao.community.entity.Message;

import java.util.List;

public interface MessageService {

    List<Message> findConversations(int userId, int offset, int limit);

    int findConversationCount(int userId);

    List<Message> findLetters(String conversationId, int offset, int limit);

    int findLetterCount(String conversationId);

    int findUnreadLetterCount(int userId, String conversationId);

    int addMessage(Message message);

    int readMessage(List<Integer> ids);

    /**
     * 找到某用户某主题下最新的通知
     *
     * @param userId 用户Id
     * @param topic  主题
     * @return 最新的通知
     */
    Message findLatestNotice(int userId, String topic);

    /**
     * 某用户某主题下的通知数量
     *
     * @param userId 用户Id
     * @param topic  主题
     * @return 通知的数量
     */
    int findNoticeCount(int userId, String topic);

    /**
     * 某用户某主题下未读消息的数量，若主题为空，则返回所有主题下未读消息的数量
     *
     * @param userId 用户Id
     * @param topic  主题名称，可空
     * @return 未读消息的数量
     */
    int findUnreadNoticeCount(int userId, String topic);

    /**
     * 查询某一用户某一主题下所有的通知
     *
     * @param userId 用户Id
     * @param topic  主题
     * @param offset 偏移
     * @param limit  每页显示的数量
     * @return 由message构成的list集合
     */
    List<Message> findNotices(int userId, String topic, int offset, int limit);

}
