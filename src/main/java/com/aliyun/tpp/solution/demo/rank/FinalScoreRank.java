package com.aliyun.tpp.solution.demo.rank;

import com.aliyun.tpp.service.rec.Rank;
import com.aliyun.tpp.solution.demo.data.RecommendContext;
import com.aliyun.tpp.solution.demo.data.ScoreItem;

import java.util.List;

/**
 * author: oe
 * date:   2021/8/31
 * comment:算出最终得分FinalScore，并根据FinalScore排序
 * 注意：这里只是个例子，不对您的业务效果负责，请根据自己的需求改造
 */
public class FinalScoreRank implements Rank<RecommendContext, ScoreItem> {
    @Override
    public List<ScoreItem> rank(RecommendContext context) {
        List<ScoreItem> list = context.getScoreItemList();
        if (list == null || list.isEmpty()) { //没有就不计算
            context.getEmptyTraceLog().put(this.getClass().getName(), "scoreItemList is empty");
            return list;
        }
        list.forEach(scoreItem -> { //召回和排序分数求平均
            scoreItem.setFinalScore(scoreItem.getMatchScore() * 0.5 + scoreItem.getRankScore() * 0.5);
        });
        return list;
    }
}
