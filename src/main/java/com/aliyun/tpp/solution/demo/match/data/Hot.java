package com.aliyun.tpp.solution.demo.match.data;

import lombok.*;
import com.aliyun.tpp.solution.demo.data.ScoreItem;

import java.util.Random;

/**
 * author: oe
 * date:   2021/8/30
 * comment:热门召回key和value
 */

@Data
public class Hot {

    private Key key;
    private Value value;

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Key {
        private String key;//F_8_7 M_4_1

        public Key(String key) {
            this.key = key;
        }

        public String buildKey(String bizId) {
            return String.format("%s\u0001%s\u0001%s\u0001%s", MatchType.hot, bizId, key, MatchType.hot);
        }
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper=true)
    public static class Value extends ScoreItem {

        public Value(String itemId, int matchRank, double matchScore) {
            super(itemId,
                    CAT[(new Random().nextInt(CAT.length))],
                    MatchType.hot.name(),
                    matchRank,
                    matchScore,
                    0.0d,
                    0.0d);
        }

        //5b2a1dbfc9bd78a081496c31f9395976\u00020.6020599913279623\u00021
        public static Value parseValue(String str) {
            if (str == null || str.isEmpty()){
                throw new IllegalArgumentException("str is empty");
            }
            String[] fields = str.split("\u0002");
            if (fields.length<3){
                throw new IllegalArgumentException("fields.length<3");
            }
            Value value = new Value(fields[0],
                    Integer.valueOf(fields[2]==null?"-1":fields[2]),
                    Double.valueOf(fields[1]==null?"0.0":fields[1]));
            return value;
        }
    }
}
