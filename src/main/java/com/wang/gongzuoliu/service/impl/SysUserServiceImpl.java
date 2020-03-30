package com.wang.gongzuoliu.service.impl;

import com.wang.gongzuoliu.entity.SysUser;
import com.wang.gongzuoliu.mapper.SysUserMapper;
import com.wang.gongzuoliu.service.ISysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author wangchen
 * @since 2020-03-16
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Autowired
    private SysUserMapper userMapper;

    public SysUser getUserByName(String username) {
        return userMapper.getUserByName(username);
    }
}
