package com.hmall.cart;

import com.heima.hmapi.config.DefaultFeignConfig;
import com.hmall.cart.config.nacosBalanceConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
@EnableFeignClients(basePackages ="com.heima.hmapi.client" ,defaultConfiguration = DefaultFeignConfig.class)
@LoadBalancerClients(defaultConfiguration = nacosBalanceConfig.class) //- 全局配置：对所有服务生效
@MapperScan("com.hmall.cart.mapper")
@SpringBootApplication
public class CartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }


    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate() ;
    }

}
