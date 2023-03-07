package com.vls.reggie.service.Impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vls.reggie.entity.User;
import com.vls.reggie.mapper.UserMapper;
import com.vls.reggie.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
