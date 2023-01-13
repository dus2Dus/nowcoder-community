package com.druh.community.service;

import com.druh.community.entity.User;
import com.druh.community.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserMapper userMapper;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

}

