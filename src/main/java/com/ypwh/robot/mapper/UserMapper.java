package com.ypwh.robot.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ypwh.robot.model.User;

@Mapper
public interface UserMapper {

    User selectUserByUsernameAndPassword(User user);

    User selectUserById(@Param("id") Integer id);

    int insertUser(User user);

}
