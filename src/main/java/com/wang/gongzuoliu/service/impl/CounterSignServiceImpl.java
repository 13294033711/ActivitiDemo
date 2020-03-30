package com.wang.gongzuoliu.service.impl;

import com.wang.gongzuoliu.common.demo.LeaderUtils;
import com.wang.gongzuoliu.service.CounterSignService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.runtime.Execution;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 会签服务
 */
@Slf4j
public class CounterSignServiceImpl implements CounterSignService {


    @Override
    public Set<String> getUsers() {
        log.info("开始获取参与会签任务人员列表");

        //此处可用从多种渠道获取用户：比如从数据库中读取节点人员配置
        Set<String> userIds=new LinkedHashSet<String>();
        userIds.add(LeaderUtils.getDirector());
        userIds.add(LeaderUtils.getDeputyDirector());
        userIds.add(LeaderUtils.getGeneralManagers().get(0));

        log.info("获取参与会签任务人员列表结束");
        return null;
    }

    @Override
    public boolean isComplete() {
        //自定义完成逻辑
        log.info("开始判断会签任务是否结束");


        log.info("会签任务已经结束");
        return false;
    }
}
