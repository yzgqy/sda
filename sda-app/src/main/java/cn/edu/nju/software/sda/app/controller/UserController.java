package cn.edu.nju.software.sda.app.controller;

import cn.edu.nju.software.sda.app.dao.UserMapper;
import cn.edu.nju.software.sda.app.entity.UserEntity;
import cn.edu.nju.software.sda.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @Auther: yaya
 * @Date: 2020/2/16 15:12
 * @Description:
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity login(@RequestBody UserEntity user) {
        String userId = userService.login(user);
        if(userId!=null)
            return ResponseEntity.ok(userId);
        else
            return ResponseEntity.status(500).body("用户名和密码不匹配！");
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity register(@RequestBody UserEntity user) {
        userService.register(user);
        return ResponseEntity.ok(user.getUsername());
    }
}
