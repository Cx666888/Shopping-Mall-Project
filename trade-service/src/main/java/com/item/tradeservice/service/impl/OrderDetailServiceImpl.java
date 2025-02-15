package com.item.tradeservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.tradeservice.domain.po.OrderDetail;
import com.item.tradeservice.mapper.OrderDetailMapper;
import com.item.tradeservice.service.IOrderDetailService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单详情表 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-05
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements IOrderDetailService {

}
