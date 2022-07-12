import com.aliyun.tpp.service.dubbo.GenericService;
import com.aliyun.tpp.service.dubbo.GenericServiceConfig;
import com.aliyun.tpp.service.inner.ServiceLoaderProvider;
import com.aliyun.tpp.service.proxy.ServiceProxyHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.Map;

/**
 * author: oe
 * date:   2022/5/31
 * comment:dubbo泛化调用
 /**
 * author: oe
 * date:   2022/5/30
 * comment:方案作为dubbo consumer，调用dubbo provider
 * <p>
 * TPP方案代码使用的一些限制：
 * 注册中心：tpp内暂时不支持注册中心，建议给您的provider添加slb，consumer调用时直接连slb即可。
 * 泛化调用：tpp只支持consumer用泛化的方式调用provider，泛化有多种类型，暂时只测试过generic="true"
 * protocol 协议：tpp暂时只测试过dubbo，且序列化不支持protobuf。
 * <p>
 * 如果您用到sae，千万要注意"sae内置的注册中心"只适用于consumer和provider都在sae的情况
 * 参考 https://dubbo.apache.org/zh/docs/advanced/generic-reference/
 */
@RunWith(JUnit4.class)
public class GenericServiceTest {
    private GenericService genericService;

    @Before
    public void before() {
        GenericServiceConfig config = new GenericServiceConfig();
        config.setApplicationName("dubbo-hello-provider");
        config.setInterfaceName("com.aliyun.tpp.web.dubbo.hello.HelloService");
        config.setVersion("1.0.0");
        config.setGeneric("true");
        //注册中心模式：先从注册中心获取provider地址，再向provider发起调用。-DDUBBO_IP_TO_REGISTRY=11.101.127.220 register.ip=注册中心ip
        //config.setRegistryAddress("nacos://127.0.0.1:8848");//-DDUBBO_IP_TO_REGISTRY=11.101.127.220 register.ip=注册中心ip
        config.setUrl("dubbo://11.101.127.220:80");
        //直连模式：绕开注册中心，直接调用provider机器的ip和port。如果您要本地测试，可以给您的provider加一个公网slb(一定要开启访问控制，否则有安全风险)，本地就直接调用slb的ip和port
        //如果provider的dubbo:protocol name=dubbo, 这里填 dubbo://${ip}:${port}
        //如果provider的dubbo:protocol name=http, 这里填 http://${ip}:${port}
        try {
            this.genericService = ServiceProxyHolder.getService(config, ServiceLoaderProvider.getSuperLoaderEasy(GenericService.class));
            this.genericService.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @After
    public void after() {
        genericService.destroy();
    }

    @Test
    public void testSayHello() throws Exception {
        Map<String, Object> HelloRequest = new HashMap<String, Object>();
        HelloRequest.put("name", "jdflsa");
        Object value = this.genericService.invoke("sayHello", new String[]{"com.aliyun.tpp.web.dubbo.hello.HelloRequest"}, new Object[]{HelloRequest});
        System.out.println(value.toString());
    }
}
