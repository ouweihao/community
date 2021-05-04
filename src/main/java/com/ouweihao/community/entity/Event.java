package com.ouweihao.community.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统发送通知的事件
 */
public class Event {

    /**
     * 事件的主题
     */
    private String topic;

    /**
     * 事件的触发人
     */
    private int userId;

    /**
     * 在什么实体上触发，是帖子，评论还是回复
     */
    private int entityType;

    /**
     * 实体的id
     */
    private int entityId;

    /**
     * 实体的作者id
     */
    private int authorId;

    /**
     * 用于业务拓展，以后有什么拓展的业务可以放到data中
     */
    private Map<String, Object> data = new HashMap<>();

    public String getTopic() {
        return topic;
    }

    /**
     * 返回是为了更好地设置参数
     *
     * @param topic 主题
     * @return event
     */
    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getAuthorId() {
        return authorId;
    }

    public Event setAuthorId(int authorId) {
        this.authorId = authorId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return "event{" +
                "topic='" + topic + '\'' +
                ", userId='" + userId + '\'' +
                ", entityType=" + entityType +
                ", entityId=" + entityId +
                ", authorId=" + authorId +
                ", data=" + data +
                '}';
    }

}
