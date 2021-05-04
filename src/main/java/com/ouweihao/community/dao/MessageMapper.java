package com.ouweihao.community.dao;

import com.ouweihao.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    // 查询当前用户的会话列表，针对每个会话只返回一条最新的私信

    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前用户的会话数量

    int selectConversationCount(int userId);

    // 查询某个会话所包含的私信列表

    List<Message> selectLetters(String conversationId, int offset, int limit);

    // 查询某个会话所包含的私信数量

    int selectLetterCount(String conversationId);

    // 查询未读私信数量

    int selectUnreadLetterCount(int userId, String conversationId);

    // 新增消息

    int insertMessage(Message message);

    // 修改消息的状态 多条消息的状态

    int updateStatus(List<Integer> ids, int status);

    /**
     * 查询某个主题下最新的通知
     *
     * @param userId 某用户
     * @param topic  主题
     * @return 某主题下最新的一个通知
     */
    Message selectLatestNotice(int userId, String topic);

    /**
     * 某用户某主题下通知的数量
     *
     * @param userId 用户Id
     * @param topic  主题
     * @return 通知数量
     */
    int selectNoticeCount(int userId, String topic);

    /**
     * 某用户某主题下未读消息的数量
     *
     * @param userId 用户Id
     * @param topic  主题
     * @return 未读消息数量
     */
    int selectUnreadNoticeCount(int userId, String topic);

    /**
     * 查询某用户某一主题下所有的系统通知
     *
     * @param userId 用户ID
     * @param topic  主题
     * @param limit  每页显示数量
     * @param offset 偏移
     * @return
     */
    List<Message> selectNotices(int userId, String topic, int offset, int limit);

}
