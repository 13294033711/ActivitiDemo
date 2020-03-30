package com.wang.gongzuoliu.listener;

import com.wang.gongzuoliu.common.demo.LeaderUtils;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class GetDeputyDirectorListener implements TaskListener {

    public void notify(DelegateTask delegateTask) {
        System.out.println("执行获取【副主任】监听器");
        //模拟查询副主任,并设置办理人
        String assignee = LeaderUtils.getDeputyDirector();
//        delegateTask.setAssignee(assignee);
        delegateTask.addCandidateUser(assignee);
    }
}
