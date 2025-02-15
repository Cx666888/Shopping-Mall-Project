package com.hmall.payservice;

import com.heima.hmapi.config.DefaultFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages ="com.heima.hmapi.client" ,defaultConfiguration = DefaultFeignConfig.class)
@MapperScan("com.hmall.payservice.mapper")
@SpringBootApplication
public class PayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayServiceApplication.class, args);
    }

}
