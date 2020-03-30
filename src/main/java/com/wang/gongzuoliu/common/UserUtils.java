package com.wang.gongzuoliu.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserUtils {

    /**
     * 获取当前用户认证信息
     * @return
     */
    public static Authentication getCurrentUserAuthentication(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前用户
     * @return
     */
    public static UserDetails getCurrentPrincipal(){
        return  (UserDetails)getCurrentUserAuthentication().getPrincipal();
    }

    /**
     * 获取当前用户登录地址
     */
    public static String getRemoteAddress() {
        WebAuthenticationDetails details = (WebAuthenticationDetails)getCurrentUserAuthentication().getDetails();
        return details.getRemoteAddress();
    }

    /**
     * 获取当前用户seesionId
     */
    public static String getSeesionId() {
        WebAuthenticationDetails details = (WebAuthenticationDetails)getCurrentUserAuthentication().getDetails();
        return details.getSessionId();
    }

    /**
     * 获取当前用户权限
     */
    public static List<String> getAuths() {
        List<String> auths = new ArrayList<>();
        Collection<? extends GrantedAuthority> authorities = getCurrentUserAuthentication().getAuthorities();
        for (GrantedAuthority g: authorities) {
            auths.add(g.getAuthority());
        }
        return auths;
    }
}
