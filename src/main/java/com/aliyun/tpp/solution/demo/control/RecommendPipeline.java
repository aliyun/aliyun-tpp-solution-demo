package com.aliyun.tpp.solution.demo.control;

import com.aliyun.tpp.service.control.Pipeline;
import com.aliyun.tpp.service.step.Step;
import com.aliyun.tpp.solution.demo.data.RecommendContext;

/**
 * author: oe
 * date:   2021/9/3
 * comment:推荐pipeline，按顺序执行每个step
 */
public class RecommendPipeline<STEP extends Step<RecommendContext, RESULT>, RESULT> extends Pipeline<STEP, RecommendContext, RESULT> {

    //每一个步骤做完后result原样返回
    @Override
    protected RESULT stepResult(RecommendContext context, RESULT result) {
        return result;
    }
}
