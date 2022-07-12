/**
 * author: oe
 * date:   2021/9/6
 * comment:
 */
package client;

import com.alibaba.fastjson.JSON;
import com.aliyun.tpp.service.be.BeClient;
import com.aliyun.tpp.service.be.BeConfig;
import com.aliyun.tpp.service.inner.ServiceLoaderProvider;
import com.aliyun.tpp.service.proxy.ServiceProxyHolder;
import com.aliyuncs.be.client.BeReadRequest;
import com.aliyuncs.be.client.BeResponse;
import com.aliyuncs.be.client.BeResult;
import com.aliyuncs.be.client.exception.InvalidParameterException;
import com.aliyuncs.be.client.protocol.BeBizType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class BeClientTest {
    private BeClient beClient;

    @Before
    public void before() {
        BeConfig beConfig = new BeConfig();
        beConfig.setPassword("tpp*****");
        beConfig.setUsername("tppuser");
        beConfig.setDomain("aime-cn-0ju*********.aime.aliyuncs.com");
        beClient = ServiceProxyHolder.getService(beConfig, ServiceLoaderProvider.getSuperLoaderEasy(BeClient.class));
        beClient.init();
    }

    @Test
    public void test() throws InvalidParameterException {
        List<String> triggers = new ArrayList<>();
        triggers.add("10103b20efca6825bdecf814790afa7e");
        triggers.add("87c7e054c0535b9d4954bba2454e3c4d");
        BeReadRequest x2iRequest = BeReadRequest.builder()
                .bizName("x2i_match")
                .bizType(BeBizType.X2I)
                .returnCount(500)
                .items(triggers)
                .build();
        BeResponse<BeResult> x2iResponse = beClient.query(x2iRequest);
        System.out.println(JSON.toJSON(x2iResponse));//x2iResponse.isSuccess() && x2iResponse.getResult().getItemsCount()>0;
        assert !x2iResponse.isSuccess();
    }


    @After
    public void after() {
        beClient.destroy();
    }
}
