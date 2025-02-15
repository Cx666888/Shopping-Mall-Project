package com.item.tradeservice.listener;

import com.item.tradeservice.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayStatusListener {
private final IOrderService iOrderService;
@RabbitListener(bindings = @QueueBinding(
     value = @Queue(name = "trade.pay.success.queue",durable = "true"),
        exchange =@Exchange(name = "pay.direct"),
        key = "pay.success"
))
public void paySuccess(Long orderId){
iOrderService.markOrderPaySuccess(orderId);
}
}
