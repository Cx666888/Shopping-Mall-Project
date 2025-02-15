package com.heima.hmapi.client;

import com.heima.hmapi.config.PayClientFallback;
import com.heima.hmapi.dto.PayOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "pay-service",fallbackFactory =PayClientFallback.class)
public interface PayClient {
    @GetMapping("/pay-orders/biz/{id}")
    PayOrderDTO queryPayOrderByBizOrderNo(@PathVariable("id") Long id);
    @GetMapping("/pay-orders/{id}/{status}")
    public void updatePayOrderByBizOrderNo(@PathVariable("id") Long id, @PathVariable ("status") Integer status);
}

