package com.aliyun.tpp.solution.demo.match;

import com.aliyun.tpp.service.be.BeClient;
import com.aliyun.tpp.service.be.BeConfig;
import com.aliyun.tpp.service.inner.ServiceLoaderProvider;
import com.aliyun.tpp.service.proxy.ServiceProxyHolder;
import com.aliyun.tpp.service.rec.Match;
import com.aliyuncs.be.client.BeReadRequest;
import com.aliyuncs.be.client.BeResponse;
import com.aliyuncs.be.client.BeResult;
import com.aliyuncs.be.client.protocol.BeBizType;
import com.aliyuncs.be.client.protocol.clause.FilterClause;
import com.aliyuncs.be.client.protocol.clause.FilterOperator;
import com.aliyuncs.be.client.protocol.clause.SingleFilter;
import lombok.Getter;
import com.aliyun.tpp.solution.demo.data.RecommendContext;

import java.util.List;

/**
 * author: oe
 * date:   2021/9/3
 * comment:BE召回
 */
public abstract class BeMatch<RESPONSE> implements Match<RecommendContext, RESPONSE> {

    @Getter
    private BeConfig beConfig;

    @Getter
    private BeClient beClient;

    public BeMatch(BeConfig beConfig) {
        this.beConfig = beConfig;
        beClient = ServiceProxyHolder.getService(beConfig, ServiceLoaderProvider.getSuperLoaderEasy(BeClient.class));//获取client
    }

    @Override
    public void init() {
        beClient.init();
    }

    @Override
    public void destroy() {
        if (beClient != null) {
            beClient.destroy();
        }
    }

    @Override
    public List<RESPONSE> match(RecommendContext context) throws Exception {
        checkParams(context);
        List<String> triggers = buildKey(context);
        BeReadRequest x2iRequest = BeReadRequest.builder()
                .bizName("x2i_match")
                .bizType(BeBizType.X2I)
                .returnCount(context.getMatchMaxNum())
                .items(triggers)
                .filter(new FilterClause(new SingleFilter(
                        "score",
                        FilterOperator.GT,
                        "'0.1D'")))
                .build();
        BeResponse<BeResult> x2iResponse = beClient.query(x2iRequest);
        return parseResponse(context, x2iResponse);
    }


    protected void checkParams(RecommendContext context) {
        if (context.getUserId() == null || context.getUserId().isEmpty()) {
            throw new IllegalArgumentException("userId is empty");
        }
    }

    //构造key
    protected abstract List<String> buildKey(RecommendContext context) throws Exception;

    /**
     * 解析response
     */
    protected abstract List<RESPONSE> parseResponse(RecommendContext context, BeResponse<BeResult> response);
}
