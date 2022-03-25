package com.aliyun.tpp.solution.demo.match;

import com.aliyun.tpp.service.be.BeConfig;
import com.aliyuncs.be.client.BeResponse;
import com.aliyuncs.be.client.BeResult;
import lombok.Getter;
import com.aliyun.tpp.solution.demo.data.RecommendContext;
import com.aliyun.tpp.solution.demo.match.data.I2i;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * author: oe
 * date:   2021/9/6
 * comment:基于be的i2i召回
 */
public class I2iBeMatch extends BeMatch<I2i.Value> {

    @Getter
    private TriggerLoader triggerLoader;

    public I2iBeMatch(BeConfig beConfig, TriggerLoader triggerLoader) {
        super(beConfig);
        this.triggerLoader = triggerLoader;
    }

    @Override
    public void init() {
        super.init();
        triggerLoader.init();
    }

    @Override
    public void destroy() {
        if (triggerLoader != null) {
            triggerLoader.destroy();
        }
        super.destroy();
    }

    @Override
    public boolean skip(RecommendContext context) {
        return !context.i2iMatchSwitch;
    }

    @Override
    protected List<String> buildKey(RecommendContext context) throws Exception {
        List<String> triggers =  triggerLoader.invoke(context)
                .stream().map((trigger)->{return context.getBizId()+"_"+trigger.getItemId();}).collect(Collectors.toList());
        context.setTriggers(triggers);
        List<String> keys = context.getTriggers().stream().map(trigger->{
            return context.getBizId()+"_"+trigger;
        }).collect(Collectors.toList());
        return keys;
    }

    @Override
    protected List<I2i.Value> parseResponse(RecommendContext context,BeResponse<BeResult> response) {
        if (response == null || !response.isSuccess()){
            context.getEmptyTraceLog().put(this.getClass().getName(), response.getMessage());
            return Collections.emptyList();
        }
        BeResult beResult = response.getResult();
        return I2i.Value.parseResult(beResult);
    }
}
