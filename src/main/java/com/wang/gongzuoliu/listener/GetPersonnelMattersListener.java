package com.wang.gongzuoliu.listener;

import com.wang.gongzuoliu.common.demo.LeaderUtils;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * 获取人事员工监听器
 */
public class GetPersonnelMattersListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        //模拟查询人事员工，并设置处理人
        String assignee = LeaderUtils.getPersonnelMatters();
//        delegateTask.setAssignee(assignee);addCandidateUser
        delegateTask.addCandidateUser(assignee);
    }
}
