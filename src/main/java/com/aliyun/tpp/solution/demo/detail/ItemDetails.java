
package com.aliyun.tpp.solution.demo.detail;

import com.aliyun.tpp.service.step.Step;
import com.aliyun.tpp.solution.demo.data.RecommendContext;
import com.aliyun.tpp.solution.demo.data.ScoreItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * author: oe
 * date:   2021/8/30
 * comment:查询item详情
 */
public class ItemDetails implements Step<RecommendContext, List<Item>> {

    /**
     * 查询item详情
     */
    public List<Item> queryItem(RecommendContext context) {
        List<String> itemIds = context.getItemIds();
        if (itemIds == null || itemIds.isEmpty()) {
            throw new IllegalArgumentException("itemIds is empty");
        }
        //convert scoreItem 2 item
        List<ScoreItem> scoreItemList = context.getScoreItemList();
        List<Item> list = new ArrayList<>(scoreItemList.size());
        if (scoreItemList != null && !scoreItemList.isEmpty()) {
            list = scoreItemList.stream().map(scoreItem -> Item.convert(scoreItem)).collect(Collectors.toList());
        }
        //mock 这里可以改成真实的详情查询 例如 List<Item> list = queryDetails(itemIds);
        list.forEach(item -> {
            int random = new Random().nextInt(10) + 1;//1~10
            item.setItemPic("item_pic");//i1/54882540/O1CN0120MTT91UdMhwrI3Qx_!!0-item_pic.jpg
            item.setItemPrice(random * 10.0);//80.0
            item.setItemTitle("item_title");//重回汉唐妙音鸟原创汉服女直领对襟衫齐腰交窬襦裙中国风夏季薄款
            item.setItemTag("item_tag" + random);//143746
        });

        return list;
    }

    @Override
    public List<Item> invoke(RecommendContext context) throws Exception {
        List<Item> detailList = queryItem(context);
        context.setItemList(detailList);
        return detailList;
    }
}
