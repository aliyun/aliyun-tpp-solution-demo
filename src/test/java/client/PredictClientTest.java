/**
 * author: oe
 * date:   2022/2/25
 * comment:
 */
package client;

import com.aliyun.tpp.service.inner.ServiceLoaderProvider;
import com.aliyun.tpp.service.predict.PredictClient;
import com.aliyun.tpp.service.predict.PredictConfig;
import com.aliyun.tpp.service.proxy.ServiceProxyHolder;
import com.aliyun.tpp.solution.predict_demo.EasyRecProtos;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.Collections;

@RunWith(JUnit4.class)
public class PredictClientTest {
    private PredictClient predictClient;

    @Before
    public void before() {
        PredictConfig config = new PredictConfig();
        config.setConnectTimeout(1000);
        config.setReadTimeout(1000);
        config.setHost("123456*********789.cn-shanghai.pai-eas.aliyuncs.com");//本地测试使用公网地址，pai-eas必须开通vpc高速直连
        config.setToken("MjU1Y**********jZQ==");
        config.setModel("easy_rec_multi_tower");
        predictClient = ServiceProxyHolder.getService(config, ServiceLoaderProvider.getSuperLoaderEasy(PredictClient.class));
        predictClient.init();
    }

    @After
    public void after() {
        if (predictClient != null) {
            predictClient.shutdown();
        }
    }

    @Test
    public void test() throws Exception {
        String[] itemIds = new String[]{"10103b20efca6825bdecf814790afa7e"};
        EasyRecProtos.PBRequest request = EasyRecProtos.PBRequest.newBuilder()
                .putAllUserFeatures(Collections.EMPTY_MAP)
                .putAllContextFeatures(Collections.EMPTY_MAP)
                .addAllItemIds(Arrays.asList(itemIds))
                .build();
        //response
        byte[] rawResponse = predictClient.predict(request.toByteArray());
        assert rawResponse != null;
    }
}
