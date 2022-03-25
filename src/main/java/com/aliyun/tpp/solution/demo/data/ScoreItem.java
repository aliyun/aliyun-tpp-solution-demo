package com.aliyun.tpp.solution.demo.data;

import com.aliyun.tpp.solution.demo.match.data.MatchType;
import lombok.*;

import java.io.Serializable;

/**
 * author: oe
 * date:   2021/8/31
 * comment:参与分数计算的item，在召回和排序阶段用到
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ScoreItem implements Serializable {
    private String itemId;
    private String catId;//类目id
    private String matchType = MatchType.EMPTY.name();//召回类型
    private int matchRank = -1;//召回时的排名
    private double matchScore = 0;//召回分数
    private double rankScore = 0;//排序分数
    private double finalScore = 0;//最终分数

    public final static String[] CAT = new String[]{"a", "b", "c", "d", "e", "f", "g"};

}
