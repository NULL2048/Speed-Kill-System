package pers.cy.speedkillsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pers.cy.speedkillsystem.dao.GoodsDao;
import pers.cy.speedkillsystem.domain.OrderInfo;
import pers.cy.speedkillsystem.domain.SksOrder;
import pers.cy.speedkillsystem.domain.SksUser;
import pers.cy.speedkillsystem.redis.RedisService;
import pers.cy.speedkillsystem.redis.SpeedKillKey;
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

    @Autowired
    private RedisService redisService;


    /**
     * 这三个操作应该是一个原子操作，所以要创建事务
     * @param user
     * @param goods
     * @return
     */
    @Transactional
    public OrderInfo speedKill(SksUser user, GoodsVo goods) {
        // 减库存
        boolean flag = goodsService.reduceStock(goods);
        // 减库存成功才生成订单
        if (flag) {
            // 下订单  写入秒杀订单  创建order_info和sks_order  这个操作也应该是一个原子操作，也要添加事务
            return orderService.createOrder(user, goods);
        } else {
            // 如果库存为空，将商品标记为无库存
            setGoodsOver(goods.getId());
            return null;
        }
    }



    /**
     * 获取秒杀结果
     * @param userId
     * @param goodsId
     * @return
     */
    public long getSpeedKillResult(Long userId, long goodsId) {
        SksOrder order = orderService.getSpeedKillOrderByUserIdGoodsId(userId, goodsId);
        if (order != null) {
            return order.getOrderId();
        } else {
            boolean isOver = getGoodsOver(goodsId);
            // 如果已经卖完了，则返回失败
            if (isOver) {
                return -1;
            // 没卖完则返回排队中
            } else {
                return 0;
            }
        }
    }

    /**
     * 标记商品已经卖完
     * @param goodsId
     */
    private void setGoodsOver(Long goodsId) {
        redisService.set(SpeedKillKey.isGoodsOver, "" + goodsId, true);
    }

    /**
     * 查询商品是否卖完
     * @param goodsId
     * @return
     */
    private boolean getGoodsOver(long goodsId) {
        return redisService.exists(SpeedKillKey.isGoodsOver, "" + goodsId);
    }
}
