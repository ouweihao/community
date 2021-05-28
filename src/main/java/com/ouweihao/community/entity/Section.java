package com.ouweihao.community.entity;

import java.util.ArrayList;
import java.util.List;

public class Section {

    private int id;
    private String name;

    private List<DiscussPost> discussPosts = new ArrayList<>();

    public Section() {
    }

    public Section(int id, String name, List<DiscussPost> discussPosts) {
        this.id = id;
        this.name = name;
        this.discussPosts = discussPosts;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DiscussPost> getDiscussPosts() {
        return discussPosts;
    }

    public void setDiscussPosts(List<DiscussPost> discussPosts) {
        this.discussPosts = discussPosts;
    }

    @Override
    public String toString() {
        return "Section{" +
                "id=" + id +
                ", name=" + name +
                ", discussPosts=" + discussPosts +
                '}';
    }
}
