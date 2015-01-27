package com.raycloud.demo.model;

/**
 * Description:
 * User: ouzhouyou@raycloud.com
 * Date: 14-2-27
 * Time: 下午11:19
 * Version: 1.0
 */
public class UserModel extends BasePojo {
    String userNick;
    String userId;

    public String getUserNick() {
        return userNick;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
