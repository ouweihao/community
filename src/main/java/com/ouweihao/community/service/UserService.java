package com.ouweihao.community.service;

import com.ouweihao.community.entity.LoginTicket;
import com.ouweihao.community.entity.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
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

    Map<String, Object> forgetPassword(String email, String verifyCode, String newPassword);

    Map<String, Object> updateEmail(String formerEmail, String updateVerifyCode,
                                    String newEmail, String activationVerifyCode);

    int updateType(int userId, int type);

    int updateStatus(int userId, int newStatus);

    /**
     * 返回用户的权限情况
     *
     * @param userId 所要查询的用户的id
     * @return 用户的权限情况
     */
    Collection<? extends GrantedAuthority> getAuthorities(int userId);
}
