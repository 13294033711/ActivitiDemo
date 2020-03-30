package com.wang.gongzuoliu.controller;

import com.wang.gongzuoliu.common.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collection;

/**
 * 登录控制
 */

@Slf4j
@Controller
public class LoginController {

    @GetMapping({"","/","/index"})
    public String index(Model model, HttpServletRequest request) {
        Authentication currentUserAuthentication = UserUtils.getCurrentUserAuthentication();
        HttpSession session = request.getSession();
        session.setAttribute("USER_INFO", currentUserAuthentication);
        session.setAttribute("USER_ROLE", UserUtils.getAuths());
        log.info("当前登录用户： "+ currentUserAuthentication.getName());
        log.info("当前登录地址： "+ UserUtils.getRemoteAddress());
        for (String s : UserUtils.getAuths()) {
            log.info(s);
        }
        return "index";
    }

//    @RequestMapping("/login")
//    public String login(User user) {
//        //添加用户认证信息
//        Subject subject = SecurityUtils.getSubject();
//        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(
//                user.getUsername(),
//                user.getPassword()
//        );
//        try {
//            //进行验证，这里可以捕获异常，然后返回对应信息
//            subject.login(usernamePasswordToken);
////            subject.checkRole("admin");
////            subject.checkPermissions("query", "add");
//        } catch (AuthenticationException e) {
//            e.printStackTrace();
//            return "账号或密码错误！";
//        } catch (AuthorizationException e) {
//            e.printStackTrace();
//            return "没有权限";
//        }
//        return "login success";
//    }
//    //注解验角色和权限
//    @RequiresRoles("admin")
//    @RequiresPermissions("add")
//    @RequestMapping("/index")
//    public String index() {
//        return "index!";
//    }
}
