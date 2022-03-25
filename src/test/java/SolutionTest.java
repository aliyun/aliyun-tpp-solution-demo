import com.alibaba.fastjson.JSON;
import com.aliyun.tpp.solution.protocol.ContextLogger;
import com.aliyun.tpp.solution.protocol.SolutionContext;
import com.aliyun.tpp.solution.protocol.recommend.RecommendResult;
import com.aliyun.tpp.solution.protocol.recommend.RecommendSolution;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.aliyun.tpp.solution.demo.DemoSolution;

import java.util.HashMap;
import java.util.Map;

/**
 * author: oe
 * date:   2021/9/3
 * comment:
 */
@RunWith(JUnit4.class)
public class SolutionTest {

    private RecommendSolution solution;

    private static final String userId="16187b9da496bb6e0ed0d028a09fbbdf";

    @Before
    public void before() {
        solution = new DemoSolution();
        solution.init(null);
    }

    @After
    public void after() {
        solution.destroy(null);
    }

    @Test
    public void test() throws Exception {
        RecommendResult result = solution.recommend(new SolutionContext() {
            @Override
            public Object getRequestParams(String s) {
                if ("userId".equals(s)){
                    return userId;
                }
                return null;
            }

            @Override
            public Map<String, Object> getRequestParamsMapCopy() {
                return new HashMap<String,Object>(){{put("userId",userId);}};
            }

            @Override
            public Map<String, Object> getAbConfigMapCopy() {
                return null;
            }

            @Override
            public String getAbConfig(String s) {
                return null;
            }

            @Override
            public ContextLogger getContextLogger() {
                return new ContextLogger(){
                    Logger LOG =  LoggerFactory.getLogger("test");
                    @Override
                    public void info(String s) {
                        LOG.info(s);
                    }

                    @Override
                    public void warn(String s) {
                        LOG.warn(s);
                    }

                    @Override
                    public void error(String s, Throwable throwable) {
                        LOG.error(s,throwable);
                    }
                };
            }

            @Override
            public long getCurrentSolutionId() {
                return 0;
            }

            @Override
            public long getCurrentAppId() {
                return 0;
            }

            @Override
            public long getCurrentAbId() {
                return 0;
            }

            @Override
            public String getRequestId() {
                return null;
            }
        });
        System.out.println(JSON.toJSONString(result.getRecommendResult()));
        assert result.getRecommendResult().size()>0;
    }
}
