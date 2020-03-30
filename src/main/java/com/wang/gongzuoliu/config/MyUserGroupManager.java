package com.wang.gongzuoliu.config;

import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MyUserGroupManager implements UserGroupManager {
    @Override
    public List<String> getUserGroups(String s) {
        return null;
    }

    @Override
    public List<String> getUserRoles(String s) {
        System.out.println("测试");
        return null;
    }

    @Override
    public List<String> getGroups() {
        return null;
    }

    @Override
    public List<String> getUsers() {
        return null;
    }
}
