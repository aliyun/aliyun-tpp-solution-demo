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
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * author: oe
 * date:   2021/8/30
 * comment:
 */
@RunWith(JUnit4.class)
public class RedisClientTest {
    private RedisClient redisClient1;

    private RedisClient redisClient;
    @Before
    public void before() {
        RedisConfig redisConfig1 = new RedisConfig();
        redisConfig1.setHost("r-u******9.redis.rds.aliyuncs.com");//本地测试公网访问
        redisConfig1.setPassword("tpp_demo_tester:tpp_demo_tester*****");//账号:密码
        redisConfig1.setPort(6379);
        redisConfig1.setTimeout(3000);
        redisConfig1.setMaxIdleConnectionCount(16);
        redisConfig1.setMaxTotalConnectionCount(64);
        redisConfig1.setDbIndex(1);
        redisClient1 = ServiceProxyHolder.getService(redisConfig1, ServiceLoaderProvider.getSuperLoaderEasy(RedisClient.class));
        redisClient1.init();
        RedisConfig redisConfig = new RedisConfig();
        redisConfig.setHost("r-u******9.redis.rds.aliyuncs.com");//本地测试公网访问
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
        redisClient1.destroy();
        redisClient.destroy();
    }

    @Test
    public void testDB0LRange() {
        List<String> valueList = redisClient.lrange("trigger\u0001tpp_rec_demo\u000116187b9da496bb6e0ed0d028a09fbbdf", 0, -1);
        assert valueList.size() > 0;
    }

    @Test
    public void testDb1() {
        String all = redisClient1.get("all");
        assert all!=null && all.length() > 0;
    }

    @Test
    public void testDB0AndDB1(){
        testDb1();
        testDB0LRange();
    }

    @Test
    public void testMultiThread(){
        List<Thread> threadList  = new ArrayList<>(100);
        Jedis jedis = redisClient1.getResource();
        for(int i=0;i<100;i++) {
           Thread item = new Thread(()->{
                Map<String,String> user = jedis.hgetAll("user");
                System.out.println(Thread.currentThread().getName()+user);
            },String.valueOf(i));
           threadList.add(item);
        }

        threadList.forEach(item->{
            item.start();
            try {
                item.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
