package com.heima.hmapi.config;

import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLogLevel(){
        return Logger.Level.BASIC;
    }
    @Bean
    public PayClientFallback payClientFallback(){return new PayClientFallback();}
    @Bean
  public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                Long userId = UserContext.getUser();
                if(userId!=null){
                    requestTemplate.header("user-info",userId.toString());
                }
            }
        };
  }

}
