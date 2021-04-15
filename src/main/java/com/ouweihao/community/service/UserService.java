package com.ouweihao.community.service;

import com.ouweihao.community.entity.LoginTicket;
import com.ouweihao.community.entity.User;

import java.util.Map;

public interface UserService {

    User findUserById(int id);

    User findUserByName(String username);

    Map<String, Object> register(User user);

    int activation(int userId, String activationCode);

    Map<String, Object> login(String username, String password, int expiredSeconds);

    void logout(String ticket);

    LoginTicket findLoginTicket(String ticket);

    int updateHeader(int userId, String headerUrl);

    int updatePassword(int userId, String password);
}
