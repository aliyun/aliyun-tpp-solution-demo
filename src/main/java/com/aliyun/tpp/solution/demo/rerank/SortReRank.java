package com.aliyun.tpp.solution.demo.rerank;

import com.aliyun.tpp.solution.demo.data.ScoreItem;
import com.aliyun.tpp.service.rec.ReRank;
import com.aliyun.tpp.solution.demo.data.RecommendContext;

import java.util.Comparator;
import java.util.List;

/**
 * author: oe
 * date:   2021/8/31
 * comment:按分数倒序，分数越高排序越靠前
 */
public class SortReRank implements ReRank<RecommendContext, ScoreItem> {

    @Override
    public boolean skip(RecommendContext context) {
        return !context.reRankSwitch;
    }

    @Override
    public List<ScoreItem> reRank(RecommendContext context) {
        List<ScoreItem> list = context.getScoreItemList();
        if (list == null || list.isEmpty()) {//没有就不排序
            context.getEmptyTraceLog().put(this.getClass().getName(), "scoreItemList is empty");
            return list;
        }
        list.sort(Comparator.comparingDouble(ScoreItem::getFinalScore).reversed());
        //按分数倒序，分数越高排序越靠前
        return list;
    }
}
