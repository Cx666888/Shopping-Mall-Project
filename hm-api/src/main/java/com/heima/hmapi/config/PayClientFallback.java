package com.heima.hmapi.config;

import org.springframework.cloud.openfeign.FallbackFactory;
/*
  这个FallbackFactory是在远程调用失败的时候自动回调吗，需要给每个feignClient配置吗
  对于关键服务或者对系统正常运行有重要影响的服务，建议配置FallbackFactory以提供降级逻辑。
例如，一个用于用户认证的服务，如果不可用，可能会导致整个系统无法正常使用用户功能，这时配置回退机制可以返回一个默认的认证状态或者提供一些临时的认证方式，以保证系统的基本功能不受影响。
如果系统对服务的可用性要求较高，或者希望在服务不可用时提供更好的用户体验，也应该配置FallbackFactory。
也就是说如果你这个调用的方法不会影响后续逻辑或者代码的执行那么就可以不配置
* */
public class PayClientFallback implements FallbackFactory {
    @Override
    public Object create(Throwable cause) {
        return null;
    }
}
