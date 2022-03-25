package com.aliyun.tpp.solution.demo.result;

import com.aliyun.tpp.solution.demo.detail.Item;
import com.aliyun.tpp.service.current.NamedThreadFactory;
import lombok.Getter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * author: oe
 * date:   2021/9/2
 * comment:返回兜底热门商品,以免开天窗。建议是缓存在内存里、(随机间隔)定期刷新的数据
 */
public class DefaultCacheResult {

    private volatile List<Item> defaultItems = new LinkedList<>();

    @Getter
    private AtomicBoolean init = new AtomicBoolean(false);

    private final ScheduledExecutorService scheduledThreadPool =
            Executors.newScheduledThreadPool(1, new NamedThreadFactory("DefaultCacheResult-pool"));

    @PostConstruct
    public void init() {
        if (init.compareAndSet(false, true)) {
            scheduledThreadPool.scheduleAtFixedRate(() -> {
                refresh();
            }, 0, 5, TimeUnit.SECONDS);
        } else {
        }
    }

    @PreDestroy
    public void destroy() {
        scheduledThreadPool.shutdown();
    }

    private synchronized void refresh() {
        if (defaultItems.isEmpty()) {
            //mock
            Item item = new Item();
            item.setItemId("b1ab4c4eece434595c13a6fe159adddf");
            item.setItemPic("i1/54882540/O1CN0120MTT91UdMhwrI3Qx_!!0-item_pic.jpg");
            item.setItemPrice(89.0);
            item.setItemTitle("重回汉唐妙音鸟原创汉服女直领对襟衫齐腰交窬襦裙中国风夏季薄款");
            defaultItems.add(item);
        }
    }

    public List<Item> getDefaultItems() {
        return defaultItems;
    }
}
