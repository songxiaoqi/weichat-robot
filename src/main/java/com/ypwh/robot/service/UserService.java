package com.ypwh.robot.service;

import org.apache.ibatis.annotations.Param;

import com.ypwh.robot.model.User;

public interface UserService {

    User selectUserByUsernameAndPassword(User user);

    User selectUserById(@Param("id") Integer id);

    int insertUser(User user);

}
