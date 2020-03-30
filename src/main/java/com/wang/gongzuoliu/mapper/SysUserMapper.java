package com.wang.gongzuoliu.mapper;

import com.wang.gongzuoliu.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 用户信息表 Mapper 接口
 * </p>
 *
 * @author wangchen
 * @since 2020-03-16
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    SysUser getUserByName(@Param("username") String username);
}
