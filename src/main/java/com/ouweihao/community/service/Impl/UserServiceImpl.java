package com.ouweihao.community.service.Impl;

import com.ouweihao.community.dao.UserMapper;
import com.ouweihao.community.entity.User;
import com.ouweihao.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;


    @Override
    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    @Override
    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }
}
