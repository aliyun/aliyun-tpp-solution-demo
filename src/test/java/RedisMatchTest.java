import com.aliyun.tpp.service.redis.RedisConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.aliyun.tpp.solution.demo.data.RecommendContext;
import com.aliyun.tpp.solution.demo.detail.UserDetails;
import com.aliyun.tpp.solution.demo.match.HotRedisMatch;
import com.aliyun.tpp.solution.demo.match.I2iRedisMatch;
import com.aliyun.tpp.solution.demo.match.RedisMatch;
import com.aliyun.tpp.solution.demo.match.TriggerLoader;

import java.util.List;

/**
 * author: oe
 * date:   2021/8/30
 * comment:
 */
@RunWith(JUnit4.class)
public class RedisMatchTest {

    private RedisMatch hotMatch;
    private RedisMatch i2iMatch;


    @Before
    public void before() {
        RedisConfig redisConfig = new RedisConfig();
        redisConfig.setHost("r-example.redis.rds.aliyuncs.com");//本地测试使用公网访问
        redisConfig.setPassword("tpp_example:tpp_example_password");//账号:密码
        i2iMatch = new I2iRedisMatch(redisConfig,new TriggerLoader(redisConfig));
        i2iMatch.init();
        hotMatch = new HotRedisMatch(redisConfig,new UserDetails());
        hotMatch.init();
    }

    @After
    public void after() {
        i2iMatch.destroy();
        hotMatch.destroy();
    }

    @Test
    public void testI2i() throws Exception{
        String userId = "16187b9da496bb6e0ed0d028a09fbbdf";
        RecommendContext context = new RecommendContext();
        context.setUserId(userId);
        List list = i2iMatch.invoke(context);
        assert list.size() > 0;
    }

    //@Test
    public void testHot()  throws Exception{
        String userId = "16187b9da496bb6e0ed0d028a09fbbdf";
        RecommendContext context = new RecommendContext();
        context.setUserId(userId);
        List list = hotMatch.invoke(context);
        assert list.size() > 0;
    }
}
