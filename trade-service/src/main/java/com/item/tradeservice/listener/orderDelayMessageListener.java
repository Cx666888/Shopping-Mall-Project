package com.item.tradeservice.listener;

import com.heima.hmapi.client.PayClient;
import com.heima.hmapi.dto.PayOrderDTO;
import com.item.tradeservice.constants.MQConstants;
import com.item.tradeservice.domain.po.Order;
import com.item.tradeservice.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

    @Component
    @RequiredArgsConstructor
    public class orderDelayMessageListener {
        private final IOrderService iOrderService;
        private final PayClient payClient;
        @RabbitListener(bindings = @QueueBinding(
                value = @Queue(name = MQConstants.DELAY_ORDER_QUEUE_NAME),
                exchange =@Exchange(name = MQConstants.DELAY_EXCHANGE_NAME,delayed = "true"),
                key = MQConstants.DELAY_ORDER_KEY
        ))
        public void listenOrderDelayMessage(Long orderId){
            Order order = iOrderService.getById(orderId);
            if(order==null||order.getStatus()!=1){
                // 订单不存在或者已经支付
                return;
            }
            // 3.未支付，需要查询支付流水状态
            PayOrderDTO payOrder = payClient.queryPayOrderByBizOrderNo(orderId);
            if(payOrder!=null&&payOrder.getStatus()==3){
                //支付成功，更新支付状态
                iOrderService.markOrderPaySuccess(orderId);
            }else {
                //支付失败，取消订单，恢复库存
                iOrderService.cancelOrder(orderId);
            }
        }
    }
