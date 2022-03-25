package client;


import com.aliyun.tpp.service.inner.ServiceLoaderProvider;
import com.aliyun.tpp.service.proxy.ServiceProxyHolder;
import com.aliyun.tpp.service.redis.RedisClient;
import com.aliyun.tpp.service.redis.RedisConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

/**
 * author: oe
 * date:   2021/8/30
 * comment:
 */
@RunWith(JUnit4.class)
public class RedisClientTest {

    private RedisClient redisClient;

    @Before
    public void before() {
        RedisConfig redisConfig = new RedisConfig();
        redisConfig.setHost("r-uf6b2490t6******pd.redis.rds.aliyuncs.com");//本地测试公网访问
        redisConfig.setPassword("tpp_demo_tester:tpp_demo_tester*****");//账号:密码
        redisConfig.setPort(6379);
        redisConfig.setTimeout(3000);
        redisConfig.setMaxIdleConnectionCount(16);
        redisConfig.setMaxTotalConnectionCount(64);
        redisClient = ServiceProxyHolder.getService(redisConfig, ServiceLoaderProvider.getSuperLoaderEasy(RedisClient.class));
        redisClient.init();
    }

    @After
    public void after() {
        redisClient.destroy();
    }

    @Test
    public void testLRange() {
        List<String> valueList = redisClient.lrange("trigger\u0001tpp_rec_demo\u000116187b9da496bb6e0ed0d028a09fbbdf", 0, -1);
        assert valueList.size() > 0;
    }

    @Test
    public void testMLRange() throws InterruptedException{
        List<String> value1List = redisClient.lrange("trigger\u0001tpp_rec_demo\u000116187b9da496bb6e0ed0d028a09fbbdf", 0, -1);
        assert value1List.stream().filter(line -> line != null).count() > 0;

        List<String> value2List = redisClient.lrange("trigger\u0001tpp_rec_demo\u000122820d08721c98dcb667e40f288af04d", 0, -1);
        assert value2List.stream().filter(line -> line != null).count() > 0;
    }

}
