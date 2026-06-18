package com.atlas.user.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.user.entity.User;
import com.atlas.user.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器 / User management controller
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;

    @GetMapping("/page")
    @RequirePermission("system:user")
    public Result<Page<User>> page(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        return Result.ok(userMapper.selectPage(new Page<>(page, size), null));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:user")
    public Result<User> getById(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail(1001, "用户不存在");
        }
        return Result.ok(user);
    }
}
