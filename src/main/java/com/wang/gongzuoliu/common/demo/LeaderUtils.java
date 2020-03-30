package com.wang.gongzuoliu.common.demo;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟查询数据库中领导
 */
public class LeaderUtils {

    /**
     *模拟查询副主任
     * @return
     */
    public static String getDeputyDirector() {
        return "fuzhuren";
    }

    /**
     * 模拟获取主任
     */
    public static String getDirector() {
        return "zhuren";
    }

    /**
     *模拟获取总经理们
     */
    public static List<String> getGeneralManagers() {
        ArrayList<String> strings = new ArrayList<>();
        strings.add("zongjingliA");
        strings.add("zongjingliB");
        return strings;
    }

    /**
     *模拟查询人事员工
     */
    public static String getPersonnelMatters() {
        return "renshi";
    }

    /**
     *模拟获取项目经理
     */
    public static String getProjectManager() {
        return "xiangmujingli";
    }
}
