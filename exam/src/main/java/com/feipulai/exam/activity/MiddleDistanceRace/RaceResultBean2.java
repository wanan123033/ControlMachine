package com.feipulai.exam.activity.MiddleDistanceRace;

import java.util.Arrays;

/**
 * created by ww on 2019/6/24.
 */
public class RaceResultBean2 {
    private String no;//组的序号（非组号，与数据库无关）
    private String[] results;//{道次,姓名,最终成绩,第一圈,第二圈,第三圈,第四圈...}

    public RaceResultBean2() {
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String[] getResults() {
        return results;
    }

    public void setResults(String[] results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "RaceResultBean2{" +
                "no='" + no + '\'' +
                ", results=" + Arrays.toString(results) +
                '}';
    }
}