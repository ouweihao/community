package com.ouweihao.community.event;

import com.alibaba.fastjson.JSONObject;
import com.ouweihao.community.entity.DiscussPost;
import com.ouweihao.community.entity.Event;
import com.ouweihao.community.entity.Message;
import com.ouweihao.community.service.DiscussPostService;
import com.ouweihao.community.service.ElasticSearchService;
import com.ouweihao.community.service.MessageService;
import com.ouweihao.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${wk.image.commend}")
    private String wkImageCommend;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    /**
     * 消费事件
     */
    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})
    public void hanldEvnet(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            LOGGER.error("消息内容为空！！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);

        if (event == null) {
            LOGGER.error("消息格式不正确！！");
            return;
        }

        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getAuthorId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        // 得到事件触发者的用户Id，用于拼接信息内容
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (event.getData() != null) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));

        messageService.addMessage(message);

    }

    /**
     * 消费发帖事件
     */
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishEvent(ConsumerRecord record) {

        if (record == null || record.value() == null) {
            LOGGER.error("消息内容为空！！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);

        if (event == null) {
            LOGGER.error("消息格式不正确！！");
            return;
        }

        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());

        // 将查询到的service作为备份存在elasticsearch中
        elasticSearchService.saveDiscussPost(post);

    }

    /**
     * 消费删帖事件
     */
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteEvent(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            LOGGER.error("消息内容为空！！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);

        if (event == null) {
            LOGGER.error("消息格式不正确！！");
            return;
        }

        elasticSearchService.deleteDiscussPost(event.getEntityId());
    }

    /**
     * 消费分享事件，生成长图
     */
    @KafkaListener(topics = {TOPIC_SHARE})
    public void handleShareEvene(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            LOGGER.error("消息内容为空！！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);

        if (event == null) {
            LOGGER.error("消息格式不正确！！");
            return;
        }

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        String cmd = wkImageCommend + " --quality 75 " + htmlUrl + " "
                + wkImageStorage + "/" + fileName + "." + suffix;

        try {
            Runtime.getRuntime().exec(cmd);
            LOGGER.info("生成长图成功：" + cmd);
        } catch (IOException e) {
            LOGGER.error("生成长图失败：" + e.getMessage());
        }

    }

}
