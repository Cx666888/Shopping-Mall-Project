package com.hmall.item;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.hmall.item.mapper")
@SpringBootApplication
public class ITEMApplication {
    public static void main(String[] args) {
        SpringApplication.run(ITEMApplication.class, args);
    }
}