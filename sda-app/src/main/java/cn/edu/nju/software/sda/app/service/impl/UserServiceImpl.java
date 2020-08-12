package cn.edu.nju.software.sda.app.service.impl;

import cn.edu.nju.software.sda.app.dao.UserMapper;
import cn.edu.nju.software.sda.app.entity.UserEntity;
import cn.edu.nju.software.sda.app.service.UserService;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Auther: yaya
 * @Date: 2020/2/16 15:15
 * @Description:
 */

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean register(UserEntity userEntity) {
        String id = Sid.nextShort();
        userEntity.setId(id);
        userEntity.setCreatedAt(new Date());
        userEntity.setUpdatedAt(new Date());
        userEntity.setFlag(1);
        userMapper.insert(userEntity);
        return true;
    }

    @Override
    public String login(UserEntity userEntity) {
        List<UserEntity> users = userMapper.select(userEntity);
        if(users.size()>0)
            return users.get(0).getId();
        else
            return null;
    }
}
