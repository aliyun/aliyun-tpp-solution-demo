/**
 * author: oe
 * date:   2021/9/7
 * comment:
 */
package cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.aliyun.tpp.solution.demo.result.DefaultCacheResult;

@RunWith(JUnit4.class)
public class CacheTest {

    @Test
    public void initTest(){
        DefaultCacheResult defaultCacheResult = new DefaultCacheResult();
        assert defaultCacheResult.getInit().get()==false;
        defaultCacheResult.init();
        assert defaultCacheResult.getInit().get()==true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                defaultCacheResult.init();
                assert defaultCacheResult.getInit().get()==true;
            }
        }).start();
    }
}
