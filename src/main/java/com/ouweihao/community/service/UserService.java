package com.ouweihao.community.service;

import com.ouweihao.community.entity.User;

import java.util.Map;

public interface UserService {

    User findUserById(int id);

    User findUserByName(String username);

    Map<String, Object> register(User user);

    int activation(int userId, String activationCode);

    Map<String, Object> login(String username, String password, int expiredSeconds);

    void logout(String ticket);
}
