package com.ypwh.robot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ypwh.robot.mapper.UserMapper;
import com.ypwh.robot.model.User;
import com.ypwh.robot.service.UserService;

@Service("userService")
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public User selectUserByUsernameAndPassword(User user) {
        return userMapper.selectUserByUsernameAndPassword(user);
    }

    @Override
    public User selectUserById(Integer id) {
        return userMapper.selectUserById(id);
    }

    @Override
    public int insertUser(User user) {
        return userMapper.insertUser(user);
    }
}
