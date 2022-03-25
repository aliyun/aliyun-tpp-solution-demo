
import com.aliyun.tpp.solution.demo.cache.FakeCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.aliyun.tpp.solution.demo.data.RecommendContext;
import com.aliyun.tpp.solution.demo.data.ScoreItem;
import com.aliyun.tpp.solution.demo.rerank.DiversifyReRank;
import com.aliyun.tpp.solution.demo.rerank.FilterReRank;
import com.aliyun.tpp.solution.demo.rerank.SortReRank;
import com.aliyun.tpp.solution.demo.rerank.filter.ExposureFilter;
import com.aliyun.tpp.solution.demo.rerank.filter.PageFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * author: oe
 * date:   2021/8/31
 * comment:
 */
@RunWith(JUnit4.class)
public class ReRankTest {

    private SortReRank sortReRank;
    private FilterReRank filterReRank;
    private DiversifyReRank diversifyReRank;

    @Before
    public void before() {
        sortReRank = new SortReRank();
        PageFilter pageFilter =new PageFilter(new FakeCache());
        ExposureFilter exposureFilter = new ExposureFilter(new FakeCache());
        filterReRank = new FilterReRank(pageFilter,exposureFilter);
        filterReRank.init();
        diversifyReRank = new DiversifyReRank();
    }

    @After
    public void after() {
        filterReRank.destroy();
    }


    @Test
    public void cpuTest(){
        RecommendContext context = new RecommendContext();
        List<ScoreItem> list = new ArrayList<ScoreItem>(){{
            add(new ScoreItem("1","a","hot",1,0.5,0.5,0.5));
            add(new ScoreItem("11","a","i2i",1,0.65,0.65,0.65));
        }};
        context.setScoreItemList(list);
        sortReRank.reRank(context);
        assert context.getScoreItemList().get(0).getFinalScore()==0.65;
    }

    @Test
    public void filterTest(){
        RecommendContext context = new RecommendContext();
        context.filterScore=0.5d;
        context.blackItemId="2";
        List<ScoreItem> list = new ArrayList<ScoreItem>(){{
            add(new ScoreItem("1","a","hot",1,0.5,0.5,0.5));
            add(new ScoreItem("2","a","hot",1,0.6,0.6,0.6));
            add(new ScoreItem("11","a","i2i",1,0.65,0.65,0.65));
        }};
        context.setScoreItemList(list);
        filterReRank.reRank(context);
        assert context.getScoreItemList().size()==1;
    }

    @Test
    public void diversityTest(){
        RecommendContext context = new RecommendContext();
        List<ScoreItem> list = new ArrayList<ScoreItem>(){{
            add(new ScoreItem("22","b","i2i",1,0.66,0.66,0.66));
            add(new ScoreItem("11","b","i2i",1,0.65,0.65,0.65));
            add(new ScoreItem("2","c","hot",1,0.6,0.6,0.6));
            add(new ScoreItem("3","c","hot",2,0.5,0.5,0.5));
            add(new ScoreItem("4","c","hot",3,0.4,0.4,0.4));
            add(new ScoreItem("1","c","hot",4,0.3,0.3,0.3));
        }};
        context.setScoreItemList(list);
        diversifyReRank.reRank(context);
        assert context.getScoreItemList().get(1).getFinalScore()==0.6;
    }
}
