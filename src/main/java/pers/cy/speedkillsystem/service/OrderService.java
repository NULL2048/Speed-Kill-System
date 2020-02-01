package pers.cy.speedkillsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pers.cy.speedkillsystem.dao.GoodsDao;
import pers.cy.speedkillsystem.dao.OrderDao;
import pers.cy.speedkillsystem.domain.OrderInfo;
import pers.cy.speedkillsystem.domain.SksOrder;
import pers.cy.speedkillsystem.domain.SksUser;
import pers.cy.speedkillsystem.vo.GoodsVo;

import java.util.Date;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderDao orderDao;

    /**
     * 通过用户ID和商品ID获得秒杀订单
     * @param userId
     * @param goodsId
     * @return
     */
    public SksOrder getSpeedKillOrderByUserIdGoodsId(long userId, long goodsId) {
        return orderDao.getSpeedKillOrderByUserIdGoodsId(userId, goodsId);
    }

    /**
     * 创建商品订单和秒杀订单操作
     * 也是一个原子操作，要添加事务
     * @param user
     * @param goods
     * @return
     */
    @Transactional
    public OrderInfo createOrder(SksUser user, GoodsVo goods) {
        // 创建商品订单
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getSksPrice());
        orderInfo.setOrderChannel(1);
        // 订单状态，0新建未支付，1已支付，2已发货，3已收货，4已退款，5已完成
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());
        long orderId = orderDao.insertOrder(orderInfo);

        // 创建秒杀订单
        SksOrder sksOrder = new SksOrder();
        sksOrder.setGoodsId(goods.getId());
        sksOrder.setOrderId(orderId);
        sksOrder.setUserId(user.getId());

        orderDao.insertSksOrder(sksOrder);

        // 返回商品订单
        return orderInfo;
    }

    public OrderInfo getOrderById(long orderId) {
        return orderDao.getOrderById(orderId);
    }
}
