package com.ouweihao.community.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

/**
 * @author owh
 */

/**
 * indexName：实体在elasticsearch库中对应的索引名
 * type：被弱化，不重要
 * shards：分片数
 * replicas：备份数
 */
@Document(indexName = "discusspost", type = "_doc", shards = 6, replicas = 3)
public class DiscussPost {

    @Id
    private int id;

    @Field(type = FieldType.Integer)
    private int userId;

    /**
     * type：指定类型
     * analyzer：存储时的分词器，希望是拆分的粒度越小越好
     * searchAnalyzer：搜索时的分词器，我们希望搜索时的粒度不要太粗或太细，这时我们需要一种粒度比较合适的分词器
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text)
    private String mdcontent;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String htmlcontent;

    //    @Field(type = FieldType.Integer)
    private int type;

    //    @Field(type = FieldType.Integer)
    private int status;

    //    @Field(type = FieldType.Date)
    private Date createTime;

    //    @Field(type = FieldType.Date)
    private Date updateTime;

    @Field(type = FieldType.Integer)
    private int commentable;

    //    @Field(type = FieldType.Integer)
    private int commentCount;

    @Field(type = FieldType.Integer)
    private int sectionId;

    @Field(type = FieldType.Integer)
    private int views;

    //    @Field(type = FieldType.Double)
    private double score;

    @Field(type = FieldType.Text)
    private String attachName;

    @Field(type = FieldType.Text)
    private String attachUrl;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMdcontent() {
        return mdcontent;
    }

    public void setMdcontent(String mdcontent) {
        this.mdcontent = mdcontent;
    }

    public String getHtmlcontent() {
        return htmlcontent;
    }

    public void setHtmlcontent(String htmlcontent) {
        this.htmlcontent = htmlcontent;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public int getCommentable() {
        return commentable;
    }

    public void setCommentable(int commentable) {
        this.commentable = commentable;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getAttachName() {
        return attachName;
    }

    public void setAttachName(String attachName) {
        this.attachName = attachName;
    }

    public String getAttachUrl() {
        return attachUrl;
    }

    public void setAttachUrl(String attachUrl) {
        this.attachUrl = attachUrl;
    }

    @Override
    public String toString() {
        return "DiscussPost{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", mdcontent='" + mdcontent + '\'' +
                ", htmlcontent='" + htmlcontent + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", commentable=" + commentable +
                ", commentCount=" + commentCount +
                ", sectionId=" + sectionId +
                ", views=" + views +
                ", score=" + score +
                ", attachName='" + attachName + '\'' +
                ", attachUrl='" + attachUrl + '\'' +
                '}';
    }
}
