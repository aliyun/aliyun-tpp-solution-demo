package com.aliyun.tpp.solution.demo.match.data;

import com.aliyuncs.be.client.BeResult;
import lombok.*;
import com.aliyun.tpp.solution.demo.data.ScoreItem;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * author: oe
 * date:   2021/8/30
 * comment:i2i召回key和value
 */
@Data
public class I2i {
    private Key key;
    private Value value;

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Key {
        private String triggerItemId;

        public Key(String triggerItemId) {
            this.triggerItemId = triggerItemId;
        }

        public String buildKey(String bizId) {
            return String.format("%s\u0001%s\u0001%s", MatchType.i2i, bizId, triggerItemId);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper=true)
    public static class Value  extends ScoreItem {
        private int commonUser;

        public Value(String itemId, int matchRank, double matchScore,int commonUser) {
            super(itemId,
                    CAT[(new Random().nextInt(CAT.length))],
                    MatchType.i2i.name(),
                    matchRank,
                    matchScore,
                    0.0d,
                    0.0d);
            this.commonUser = commonUser;
        }

        //redis parse e058ce50b9020006a43a6a63e441bcc1\u00021.0\u00021\u00023
        public static Value parseValue(String str) {
            if (str == null || str.isEmpty()){
                throw new IllegalArgumentException("str is empty");
            }
            String[] fields = str.split("\u0002");
            if (fields.length<4){
                throw new IllegalArgumentException("fields.length<4");
            }
            Value value = new Value(fields[0],
                    Integer.valueOf(fields[2]==null?"-1":fields[2]),
                    Double.valueOf(fields[1]==null?"0.0":fields[1]),
                    Integer.valueOf(fields[3]==null?"0":fields[3]));
            return value;
        }

        //be parse
        public static List<Value> parseResult( BeResult beResult){
            List<String> fieldNames = beResult.getMatchItems().getFieldNames();
            int itemIdIndex=-1;
            int similarityScoreIndex=-1;
            for (int i=0;i<fieldNames.size();i++){
                if ("item_id_j".equals(fieldNames.get(i))){
                    itemIdIndex=i;
                }
                if ("similarity_score".equals(fieldNames.get(i))){
                    similarityScoreIndex=i;
                }
            }
            int finalItemIdIndex = itemIdIndex,finalSimilarityScoreIndex=similarityScoreIndex;
            List<List<String>> fieldValues =beResult.getMatchItems().getFieldValues();
            List<Value> list = fieldValues.stream().map(row->{
                return new Value(row.get(finalItemIdIndex),  0,Double.parseDouble(row.get(finalSimilarityScoreIndex)),0);
            }).collect(Collectors.toList());
            return list;
        }
    }
}
