/**
 * author: oe
 * date:   2022/2/25
 * comment:
 */
package client;

import com.aliyun.tpp.service.http.HttpClient;
import com.aliyun.tpp.service.http.HttpConfig;
import com.aliyun.tpp.service.inner.ServiceLoaderProvider;
import com.aliyun.tpp.service.proxy.ServiceProxyHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.nio.charset.StandardCharsets;

@RunWith(JUnit4.class)
public class HttpClientTest {
    private HttpClient httpClient;

    @Before
    public void before(){
        HttpConfig httpConfig = new HttpConfig();
        httpClient = ServiceProxyHolder.getService(httpConfig, ServiceLoaderProvider.getSuperLoaderEasy(HttpClient.class));
        httpClient.init();
    }

    @After
    public void after(){
        if (httpClient != null){
            httpClient.closeQuietly();
        }
    }

    @Test
    public void test() throws Exception{
        String response = httpClient.get("http://www.aliyun.com/", StandardCharsets.UTF_8,null);
        assert response != null;

    }
}
