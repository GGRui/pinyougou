package com.pinyougou.manage.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/login")
@RestController
public class LoginController {

    /**
     * 获取当前登录用户的信息
     * @return 用户信息
     */
    @GetMapping("/getUsername")
    public Map<String, Object> getUsername(){
        Map<String, Object> map = new HashMap<>();
        //只要使用了security的系统都可以通过SecurityContextHolder获取登录的用户信息
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 获取当前用户的角色权限---》ROLE_USER
        //SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        map.put("username", username);
        return map;
    }
}
