package com.wang.gongzuoliu.mapper;

import com.wang.gongzuoliu.entity.SysUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户和角色关联表 Mapper 接口
 * </p>
 *
 * @author wangchen
 * @since 2020-03-16
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

}
