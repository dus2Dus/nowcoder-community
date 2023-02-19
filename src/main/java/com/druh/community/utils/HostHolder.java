package com.druh.community.utils;

import com.druh.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * @author DJY
 * @date 2023/2/19 10:35
 * @apiNote 持有用户信息,用于代替session对象.
 */
@Component
public class HostHolder {

    ThreadLocal<User> userHolder = new ThreadLocal<>();

    public User getUser() {
        return userHolder.get();
    }

    public void setUser(User user) {
        userHolder.set(user);
    }

    public void removeUser() {
        userHolder.remove();
    }

}
