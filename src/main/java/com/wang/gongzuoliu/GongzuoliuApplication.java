package com.wang.gongzuoliu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
//@SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
//@EnableAutoConfiguration(exclude = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
public class GongzuoliuApplication {

    public static void main(String[] args) {
        SpringApplication.run(GongzuoliuApplication.class, args);
    }

}
