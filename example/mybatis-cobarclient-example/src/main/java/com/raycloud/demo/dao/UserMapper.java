package com.raycloud.demo.dao;

import com.raycloud.demo.model.UserModel;
import java.util.List;

/**
 * Description:XML形式
 * User: ouzhouyou@raycloud.com
 * Date: 14-6-14
 * Time: 下午2:52
 * Version: 1.0
 */
public interface UserMapper {

    UserModel getUserByUsername(String username);

    List<UserModel> getUserList();

    void insertUser(UserModel account);

    void updateUser(UserModel account);

    void deleteUser(String username);
}
