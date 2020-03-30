package com.wang.gongzuoliu.service;

import org.activiti.engine.runtime.Execution;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 会签服务接口
 */
@Service
public interface CounterSignService {

    /**
     * 获得会签任务中的人员计算集合
     *
     * @param execution
     * @return
     */
    Set<String> getUsers();

    /**
     * 会签是否计算完成
     *
     * @param execution
     * @return
     */
    boolean isComplete();
}