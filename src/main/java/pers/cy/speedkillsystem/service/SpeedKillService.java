package pers.cy.speedkillsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pers.cy.speedkillsystem.dao.GoodsDao;
import pers.cy.speedkillsystem.domain.OrderInfo;
import pers.cy.speedkillsystem.domain.SksOrder;
import pers.cy.speedkillsystem.domain.SksUser;
import pers.cy.speedkillsystem.vo.GoodsVo;

@Service
public class SpeedKillService {
    // 有一个开发习惯要养成，在自己的service中只能调用自己的dao，所以下面这样的写法是有问题的。
    // 所以这里如果我们想使用查询goods的操作，可以调用GoodsService
//    @Autowired
//    private GoodsDao goodsDao;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;


    /**
     * 这三个操作应该是一个原子操作，所以要创建事务
     * @param user
     * @param goods
     * @return
     */
    @Transactional
    public OrderInfo speedKill(SksUser user, GoodsVo goods) {
        // 减库存
        goodsService.reduceStock(goods);

        // 下订单  写入秒杀订单  创建order_info和sks_order  这个操作也应该是一个原子操作，也要添加事务
        return orderService.createOrder(user, goods);
    }
}
