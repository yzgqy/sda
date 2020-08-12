package cn.edu.nju.software.sda.app.service;

import cn.edu.nju.software.sda.app.entity.UserEntity;

/**
 * @Auther: yaya
 * @Date: 2020/2/16 15:13
 * @Description:
 */
public interface UserService {
    boolean register(UserEntity userEntity);
    String login(UserEntity userEntity);
}
