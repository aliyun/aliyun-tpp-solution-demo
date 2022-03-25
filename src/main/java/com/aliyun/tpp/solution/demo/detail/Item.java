
package com.aliyun.tpp.solution.demo.detail;

import lombok.*;
import com.aliyun.tpp.solution.demo.data.ScoreItem;

/**
 * author: oe
 * date:   2021/8/31
 * comment:最终返回的 item,多了一些item详情
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString(callSuper = true)
@EqualsAndHashCode
public class Item extends ScoreItem {
    private String itemTitle;
    private double itemPrice;
    private String itemPic;
    private String itemTag;

    public static Item convert(ScoreItem scoreItem) {
        if (scoreItem != null) {
            Item item = new Item();
            item.setItemId(scoreItem.getItemId());
            item.setCatId(scoreItem.getCatId());
            item.setMatchScore(scoreItem.getMatchScore());
            item.setRankScore(scoreItem.getRankScore());
            item.setFinalScore(scoreItem.getFinalScore());
            item.setMatchRank(scoreItem.getMatchRank());
            item.setMatchType(scoreItem.getMatchType());
            return item;
        }
        return null;
    }
}
