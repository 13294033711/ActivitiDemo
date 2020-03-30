package com.wang.gongzuoliu.listener;

import com.wang.gongzuoliu.common.demo.LeaderUtils;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 总经理监听器
 */
public class GetGeneralManagerListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        //模拟获取总经理，并设置候选人
        List<String> candidateUsers = LeaderUtils.getGeneralManagers();
        delegateTask.addCandidateUsers(candidateUsers);
    }

}
