package com.ouweihao.community.dao;

import com.ouweihao.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {

    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(int id, int status);

    int updateHeader(int id, String headerUrl);

    int updatePassword(int id, String password);

    int updateEmail(int id, String email);

    int updateType(int userId, int newType);

    int getUserCount();

    List<User> selectUsers(int offset, int limit);

}
