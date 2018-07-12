package com.cj.shop.service.consume;

import com.alibaba.fastjson.JSON;
import com.cj.shop.api.entity.OrderDetailWithBLOBs;
import com.cj.shop.api.entity.OrderMq;
import com.cj.shop.api.response.dto.OrderDto;
import com.cj.shop.dao.mapper.OrderMapper;
import com.cj.shop.service.cfg.JedisCache;
import com.cj.shop.service.impl.GoodsExtensionService;
import com.cj.shop.service.impl.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author yuchuanWeng(wycmiko @ foxmail.com)
 * @date 2018/7/10
 * @since 1.0
 */
@Component
@Slf4j
public class MessageConsumer {


    /**
     * 此处用于监听订单的状态：
     * 1、订单未付款30分钟自动关闭
     * 2、订单未签收15天后自动签收
     *
     * @param message
     */
    @RabbitListener(queues = "order-status")
    @RabbitHandler
    public void process(String message) {
        try {
            log.info("订单定时回溯 判断状态 消息接受的内容:{}", message);
            final OrderMq orderMq = JSON.parseObject(message, OrderMq.class);
            switch (orderMq.getType()) {
                case 1:
                    //30分钟未付款校验
                    payStatusCheck(orderMq.getOrderNum());
                    break;
                case 2:
                    //15天后自动确认收货校验
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderService orderService;
    @Autowired
    private GoodsExtensionService goodsExtensionService;
    private static final ReentrantLock lock = new ReentrantLock();
    @Autowired
    private JedisCache jedisCache;

    /**
     * 30min 未付款自动关闭订单
     *
     * @param orderNum
     */
    @Transactional(rollbackFor = Exception.class)
    public void payStatusCheck(String orderNum) {
        try {
            lock.lock();
            OrderDto orderDto = orderMapper.selectByOrderNum(orderNum, null);
            if (orderDto != null) {
                if (orderDto.getOrderStatus() == 1) {
                    //关闭订单
                    OrderDetailWithBLOBs bloBs = new OrderDetailWithBLOBs();
                    bloBs.setOrderNum(orderNum);
                    bloBs.setOrderStatus(5);
                    String s = orderService.updateOrderStatus(bloBs);
                    log.info("订单:{} 超时取消, 结果:{}", orderNum, s);
                }
            } else {
                log.info("订单：{} 状态 无须修改", orderNum);
            }
        } finally {
            lock.unlock();
        }
    }
}
