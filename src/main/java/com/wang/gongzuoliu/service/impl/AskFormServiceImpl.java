package com.wang.gongzuoliu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.gongzuoliu.entity.AskForm;
import com.wang.gongzuoliu.entity.SysUser;
import com.wang.gongzuoliu.mapper.AskFormMapper;
import com.wang.gongzuoliu.mapper.SysUserMapper;
import com.wang.gongzuoliu.service.IAskFormService;
import com.wang.gongzuoliu.service.ISysUserService;
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
public class AskFormServiceImpl extends ServiceImpl<AskFormMapper, AskForm> implements IAskFormService {

}
