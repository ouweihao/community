package com.ouweihao.community.service;

import com.ouweihao.community.entity.User;

public interface UserService {

    User findUserById(int id);

    User findUserByName(String username);

}
