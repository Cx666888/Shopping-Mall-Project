package com.item.tradeservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.hmapi.client.CartClient;
import com.heima.hmapi.client.ItemClient;
import com.heima.hmapi.client.PayClient;
import com.heima.hmapi.dto.ItemDTO;
import com.heima.hmapi.dto.OrderDetailDTO;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.utils.UserContext;
import com.item.tradeservice.constants.MQConstants;
import com.item.tradeservice.domain.dto.OrderFormDTO;
import com.item.tradeservice.domain.po.Order;
import com.item.tradeservice.domain.po.OrderDetail;
import com.item.tradeservice.mapper.OrderMapper;
import com.item.tradeservice.service.IOrderDetailService;
import com.item.tradeservice.service.IOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-05
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final ItemClient itemClient;
    private final IOrderDetailService detailService;
    private final PayClient payClient;
    private final CartClient cartClient;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @GlobalTransactional
    public Long createOrder(OrderFormDTO orderFormDTO) {
        // 1.订单数据
        Order order = new Order();
        // 1.1.查询商品
        List<OrderDetailDTO> detailDTOS = orderFormDTO.getDetails();
        // 1.2.获取商品id和数量的Map
        Map<Long, Integer> itemNumMap = detailDTOS.stream()
                .collect(Collectors.toMap(OrderDetailDTO::getItemId, OrderDetailDTO::getNum));
        Set<Long> itemIds = itemNumMap.keySet();
        // 1.3.查询商品
        List<ItemDTO> items = itemClient.queryItemByIds(itemIds);
        if (items == null || items.size() < itemIds.size()) {
            throw new BadRequestException("商品不存在");
        }
        // 1.4.基于商品价格、购买数量计算商品总价：totalFee
        int total = 0;
        for (ItemDTO item : items) {
            total += item.getPrice() * itemNumMap.get(item.getId());
        }
        order.setTotalFee(total);
        // 1.5.其它属性
        order.setPaymentType(orderFormDTO.getPaymentType());
        order.setUserId(UserContext.getUser());
        order.setStatus(1);
        // 1.6.将Order写入数据库order表中
        save(order);

        // 2.保存订单详情
        List<OrderDetail> details = buildDetails(order.getId(), items, itemNumMap);
        detailService.saveBatch(details);

        // 3.清理购物车商品
        cartClient.deleteCartItemByIds((List<Long>) new ArrayList<>(itemIds));

        // 4.扣减库存
        try {
            itemClient.deductStock(detailDTOS);
        } catch (Exception e) {
            throw new RuntimeException("库存不足！");
        }

        //5.发送延迟消息到MQ，等待某一段时间的延迟
        rabbitTemplate.convertAndSend(MQConstants.DELAY_EXCHANGE_NAME, MQConstants.DELAY_ORDER_KEY, order.getId(),
                message -> {
                    message.getMessageProperties().setDelay(10000000);
                    return message;
                });

        return order.getId();
    }
    @Override
    public void markOrderPaySuccess(Long orderId) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(2);
        order.setPayTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public void cancelOrder(Long orderId) {
        //修改交易订单为已关闭
        /*LambdaUpdateWrapper<Object> lambdaUpdate = Wrappers.lambdaUpdate();     Wrappers.lambdaUpdate()是静态方法，不可调用非静态方法*/
        LambdaUpdateWrapper<Order> lambda = new UpdateWrapper<Order>().lambda();
        lambda.set(Order::getStatus, 5)
                .eq(Order::getId, orderId);
        payClient.updatePayOrderByBizOrderNo(orderId,2);
        List<OrderDetail> orderDetails = detailService.lambdaQuery().eq(OrderDetail::getOrderId
                , orderId).list();
        List<OrderDetailDTO> dtos = BeanUtil.copyToList(orderDetails, OrderDetailDTO.class);
          itemClient.deductStock(dtos);
    }

    private List<OrderDetail> buildDetails(Long orderId, List<ItemDTO> items, Map<Long, Integer> numMap) {
        List<OrderDetail> details = new ArrayList<>(items.size());
        for (ItemDTO item : items) {
            OrderDetail detail = new OrderDetail();
            detail.setName(item.getName());
            detail.setSpec(item.getSpec());
            detail.setPrice(item.getPrice());
            detail.setNum(numMap.get(item.getId()));
            detail.setItemId(item.getId());
            detail.setImage(item.getImage());
            detail.setOrderId(orderId);
            details.add(detail);
        }
        return details;
    }
}
