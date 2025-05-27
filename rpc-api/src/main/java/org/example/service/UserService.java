package org.example.service;


import org.example.annotation.Retryable;
import org.example.pojo.User;



public interface UserService {

    // 查询
    @Retryable
    User getUserByUserId(Integer id);

    // 新增
    @Retryable
    Integer insertUserId(User user);
}
