
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.aliyun.tpp.solution.demo.data.RecommendContext;
import com.aliyun.tpp.solution.demo.data.ScoreItem;
import com.aliyun.tpp.solution.demo.rank.FinalScoreRank;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * author: oe
 * date:   2021/8/31
 * comment:
 */
@RunWith(JUnit4.class)
public class FinalScoreRankTest {


    private FinalScoreRank finalScoreRank = new FinalScoreRank();

    @Test
    public void rankTest(){
        List<ScoreItem> itemIds = new ArrayList<ScoreItem>(){
            {
                add(new ScoreItem("6d4a9cac331edc18fa02e6cb3618eaae","a","hot",0,0.987,0.625,0.0));
                add(new ScoreItem("c419566305cbf50b705876736a44285f","b","i2i",0,0.387,0.625,0.0));
                add(new ScoreItem("bc895dcc8d7cc58d8a75b4b49834c5ad","c","i2i",0,0.687,0.425,0.0));
            }
        };
        RecommendContext context = new RecommendContext();
        context.setScoreItemList(itemIds);
        List<ScoreItem>  list = finalScoreRank.rank(context);
        assert list!=null&&list.size()==3&&list.stream().sorted(Comparator.comparing(ScoreItem::getFinalScore)).findFirst().get().getMatchScore()==0.387;
    }
}
