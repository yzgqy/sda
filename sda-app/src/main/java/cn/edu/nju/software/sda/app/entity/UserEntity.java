package cn.edu.nju.software.sda.app.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @Auther: yaya
 * @Date: 2020/2/15 13:46
 * @Description:
 */
@Data
@NoArgsConstructor
@Table(name = "user")
public class UserEntity {
    @Id
    private String id;

    private String username;

    private String password;

    private Date createdAt;

    private Date updatedAt;

    private Integer flag = 1;

}
