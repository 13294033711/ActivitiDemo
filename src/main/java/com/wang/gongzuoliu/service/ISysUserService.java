package com.wang.gongzuoliu.service;

import com.wang.gongzuoliu.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author wangchen
 * @since 2020-03-16
 */
public interface ISysUserService extends IService<SysUser> {

    public SysUser getUserByName(String userName);
}
