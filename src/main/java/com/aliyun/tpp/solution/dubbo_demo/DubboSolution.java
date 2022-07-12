/**
 * author: oe
 * date:   2022/5/30
 * comment:方案中调用dubbo
 * 参考 https://dubbo.apache.org/zh/docs/advanced/generic-reference/
 */
package com.aliyun.tpp.solution.dubbo_demo;

import com.aliyun.tpp.service.dubbo.GenericService;
import com.aliyun.tpp.service.dubbo.GenericServiceConfig;
import com.aliyun.tpp.service.inner.ServiceLoaderProvider;
import com.aliyun.tpp.service.proxy.ServiceProxyHolder;
import com.aliyun.tpp.solution.protocol.SolutionContext;
import com.aliyun.tpp.solution.protocol.SolutionProperties;
import com.aliyun.tpp.solution.protocol.recommend.RecommendResult;
import com.aliyun.tpp.solution.protocol.recommend.RecommendResultSupport;
import com.aliyun.tpp.solution.protocol.recommend.RecommendSolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * author: oe
 * date:   2022/5/30
 * comment:方案作为dubbo consumer，调用dubbo provider
 * <p>
 * TPP方案代码使用的一些限制：
 * 注册中心：tpp内暂时不支持注册中心/配置中心/元数据中心，建议给您的provider添加slb，consumer调用时直接连slb即可。
 * 泛化调用：tpp只支持consumer用泛化的方式调用provider，泛化有多种类型，暂时只测试过generic="true"
 * 协议：tpp暂时只测试过dubbo，且序列化不支持protobuf。
 * <p>
 * 参考 https://dubbo.apache.org/zh/docs/advanced/generic-reference/
 */
public class DubboSolution implements RecommendSolution {

    private static final Logger LOGGER = LoggerFactory.getLogger(DubboSolution.class);

    private GenericService genericService;

    @Override
    public void init(SolutionProperties solutionProperties) {
        GenericServiceConfig config = new GenericServiceConfig();
        config.setApplicationName("dubbo-hello-provider");
        config.setInterfaceName("com.aliyun.tpp.web.dubbo.hello.HelloService");
        config.setVersion("1.0.0");
        config.setGeneric("true");
        config.setUrl("dubbo://10.*.*.*:80");//注意，地址必须是VPC内地址
        //直连模式：绕开注册中心，直接调用provider机器的ip和port。如果您要本地测试，可以给您的provider加一个公网slb(一定要开启访问控制，否则有安全风险)，本地就直接调用slb的ip和port
        //如果provider的dubbo:protocol name=dubbo, 这里填 dubbo://${ip}:${port}
        //如果provider的dubbo:protocol name=http, 这里填 http://${ip}:${port}
        try {
            this.genericService = ServiceProxyHolder.getService(config, ServiceLoaderProvider.getSuperLoaderEasy(GenericService.class));
            this.genericService.init();
        } catch (Exception e) {
            LOGGER.error("init error",e);
            throw new RuntimeException("init error",e);
        }
    }

    @Override
    public void destroy(SolutionProperties solutionProperties) {
        if (this.genericService!=null){
            this.genericService.destroy();
        }
    }

    @Override
    public RecommendResult recommend(SolutionContext context) throws Exception {
        //response
        RecommendResultSupport result = new RecommendResultSupport();
        List<Object> values = new LinkedList<>();
        result.setRecommendResult(values);
        try {
            Map<String, Object> HelloRequest = new HashMap<String, Object>();
            HelloRequest.put("name", context.getRequestParams("name"));
            Object value = this.genericService.invoke("sayHello", new String[]{"com.aliyun.tpp.web.dubbo.hello.HelloRequest"}, new Object[]{HelloRequest});
            values.add(value==null?"sayHello returns null":value);
        }catch (Exception e){// 如果不catch 需要上游兜底
            context.getContextLogger().error("recommend error",e);//打印报错日志，会展示在场景->日志分析->用户自定义日志
            LOGGER.error("recommend error",e);//打印报错日志，会展示在场景->日志分析->异常
        } finally {
            //标记空结果，会展示在场景->日志分析->空结果
            result.setEmpty(result.getRecommendResult() == null || result.getRecommendResult().isEmpty());
        }
        return result;
    }
}
