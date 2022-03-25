import com.aliyun.tpp.service.abfs.ABFSConfig;
import com.aliyun.tpp.service.predict.PredictConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.aliyun.tpp.solution.demo.data.RecommendContext;
import com.aliyun.tpp.solution.demo.data.ScoreItem;
import com.aliyun.tpp.solution.demo.rank.PaiEasRank;
import com.aliyun.tpp.solution.demo.rank.EasyRecRank;

import java.util.ArrayList;
import java.util.List;

/**
 * author: oe
 * date:   2021/8/31
 * comment:
 */
@RunWith(JUnit4.class)
public class PaiEasRankTest {


    private PaiEasRank paiEasRank;


    @Before
    public void before() {
        ABFSConfig abfsConfig = new ABFSConfig();
        abfsConfig.setSrc("tpp_rec_demo");
        abfsConfig.setEndpoint("abfs-cn-abc123.abfs.aliyuncs.com");
        abfsConfig.setUserName("tpp_test_user");
        abfsConfig.setUserPasswd("tpp_example_password");
        PredictConfig predictConfig = new PredictConfig();
        predictConfig.setHost("example_host.cn-shanghai.pai-eas.aliyuncs.com");//本地测试使用公网地址，pai-eas必须开通vpc高速直连
        predictConfig.setToken("example__token");
        predictConfig.setModel("easy__rec__example");
        paiEasRank = new EasyRecRank(predictConfig, abfsConfig);
        paiEasRank.init();
    }

    @After
    public void after() {
        paiEasRank.destroy();
    }

    @Test
    public void rankTest() throws Exception {
        List<String> itemIds = new ArrayList<String>() {
            {
                add("6d4a9cac331edc18fa02e6cb3618eaae");
                add("c419566305cbf50b705876736a44285f");
                add("bc895dcc8d7cc58d8a75b4b49834c5ad");
            }
        };
        RecommendContext context = new RecommendContext();
        context.setItemIds(itemIds);
        paiEasRank.rank(context);
        List<ScoreItem> list = context.getScoreItemList();
        System.out.println(list.size());
        assert list != null && list.size() == 3 && list.get(0).getRankScore() > 0;
    }
}
