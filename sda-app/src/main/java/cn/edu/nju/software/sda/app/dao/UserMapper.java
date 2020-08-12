package cn.edu.nju.software.sda.app.dao;

import cn.edu.nju.software.sda.app.entity.UserEntity;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/**
 * @Auther: yaya
 * @Date: 2020/2/15 13:48
 * @Description:
 */
@Repository
public interface UserMapper extends Mapper<UserEntity> {
}
