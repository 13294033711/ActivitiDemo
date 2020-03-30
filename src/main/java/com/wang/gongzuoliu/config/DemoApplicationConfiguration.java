/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wang.gongzuoliu.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
//@EnableWebSecurity
public class DemoApplicationConfiguration extends WebSecurityConfigurerAdapter {

    private Logger logger = LoggerFactory.getLogger(DemoApplicationConfiguration.class);

    @Autowired
    private MyLogoutSuccessHandler myLogoutSuccessHandler;

    /**
     * 配置身份认证
     * @param auth
     * @throws Exception
     */
    @Override
    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailsService());
    }


    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/static/**");
    }

    /**
     * 查询用户信息
     * @return
     */
    @Bean
    public UserDetailsService myUserDetailsService() {

        InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager();

        //构造用户信息
        String[][] usersGroupsAndRoles = {
                {"salaboy", "password", "ROLE_ACTIVITI_USER", "GROUP_activitiTeam"},
                {"ryandawsonuk", "password", "ROLE_ACTIVITI_USER", "GROUP_activitiTeam"},
                {"erdemedeiros", "password", "ROLE_ACTIVITI_USER", "GROUP_activitiTeam"},
                {"wangchen", "password", "ROLE_ACTIVITI_USER", "GROUP_activitiTeam"},
                {"xiangmujingli", "password", "ROLE_ACTIVITI_USER", "ROLE_MANAGE_USER","GROUP_activitiTeam"},
                {"zongjingliA", "password", "ROLE_ACTIVITI_USER", "ROLE_MANAGE_USER","GROUP_activitiTeam"},
                {"zongjingliB", "password", "ROLE_ACTIVITI_USER", "ROLE_MANAGE_USER","GROUP_activitiTeam"},
                {"zhuren", "password", "ROLE_ACTIVITI_USER", "ROLE_MANAGE_USER","GROUP_activitiTeam"},
                {"fuzhuren", "password", "ROLE_ACTIVITI_USER", "ROLE_MANAGE_USER","GROUP_activitiTeam"},
                {"renshi", "password", "ROLE_ACTIVITI_USER", "ROLE_MANAGE_USER","GROUP_activitiTeam"},
                {"other", "password", "ROLE_ACTIVITI_USER", "GROUP_otherTeam"},
                {"admin", "password", "ROLE_ACTIVITI_ADMIN"},
        };

        for (String[] user : usersGroupsAndRoles) {
            List<String> authoritiesStrings = Arrays.asList(Arrays.copyOfRange(user, 2, user.length));
            logger.info("> Registering new user: " + user[0] + " with the following Authorities[" + authoritiesStrings + "]");
            inMemoryUserDetailsManager.createUser(new User(user[0], passwordEncoder().encode(user[1]),
                    authoritiesStrings.stream().map(s -> new SimpleGrantedAuthority(s)).collect(Collectors.toList())));
        }


        return inMemoryUserDetailsManager;
    }

    /**
     * 请求过滤
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
            .headers()
                .contentTypeOptions().disable()
                .frameOptions()
                .sameOrigin()
                .and()
            .csrf().disable()
            .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
            .formLogin()
                .and()
            .logout()
//                .logoutSuccessHandler(myLogoutSuccessHandler)
                .logoutUrl("/logout").logoutSuccessUrl("/login")
                .and()
            .sessionManagement().maximumSessions(1);

    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
