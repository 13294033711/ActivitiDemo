package com.wang.gongzuoliu.controller;


import com.wang.gongzuoliu.entity.SysUser;
import com.wang.gongzuoliu.service.ISysUserService;
import com.wang.gongzuoliu.service.impl.SysUserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 用户信息表 前端控制器
 * </p>
 *
 * @author wangchen
 * @since 2020-03-16
 */
@RestController
@RequestMapping("/sys-user")
public class SysUserController {

    @Autowired
    private ISysUserService sysUserService;

    @RequestMapping("list")
    @ResponseBody
    public List<SysUser> list() {
        return sysUserService.list();
    }
}
