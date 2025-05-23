package Common.service;

import Common.pojo.User;

public interface UserService {
    // 根据ID查询ID
    User getUserByUserId(Integer id);
    // 新增
    Integer insertUserId(User user);
}
