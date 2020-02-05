package pers.cy.speedkillsystem.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pers.cy.speedkillsystem.domain.OrderInfo;
import pers.cy.speedkillsystem.domain.SksOrder;
import pers.cy.speedkillsystem.domain.SksUser;
import pers.cy.speedkillsystem.redis.RedisService;
import pers.cy.speedkillsystem.result.CodeMsg;
import pers.cy.speedkillsystem.result.Result;
import pers.cy.speedkillsystem.service.GoodsService;
import pers.cy.speedkillsystem.service.OrderService;
import pers.cy.speedkillsystem.service.SpeedKillService;
import pers.cy.speedkillsystem.vo.GoodsVo;

/**
 * 消息队列接收者/消费者
 */
@Service
public class MQReceiver {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService oderService;

    @Autowired
    private SpeedKillService speedKillService;

    @Autowired
    private RedisService redisService;

    private static Logger logger = LoggerFactory.getLogger(MQReceiver.class);

    // 该注解指定该接收者负责监听的消息队列
    @RabbitListener(queues=MQConfig.SPEED_KILL_QUEUE)
    public void receive(String message) {
        logger.info("receive message:" + message);
        SpeedKillMessage skMsg = RedisService.stringToBean(message, SpeedKillMessage.class);
        SksUser user = skMsg.getUser();
        long goodsId = skMsg.getGoodsId();

        // 判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0) {
            // 秒杀结束，没有库存了
            return;
        }

        // 判断用户是否已经秒杀过了，防止用户多次秒杀  其实这一步也不需要判断，因为我们已经在数据库做了唯一索引，所以如果不是唯一的数据也是插不进去的
        SksOrder order = oderService.getSpeedKillOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            // 不能重复秒杀
            return;
        }

        // 减库存  下订单  写入秒杀订单   这三个操作必须是一个事务原子操作，所以都写到speedKillService的方法中
        OrderInfo orderInfo = speedKillService.speedKill(user, goods);

    }


    /*
    // 该注解指定该接收者负责监听的消息队列
    @RabbitListener(queues=MQConfig.QUEUE)
    public void receive(String message) {
        logger.info("receive message:" + message);
    }

    @RabbitListener(queues=MQConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String message) {
        logger.info("topic queue1 receive message:" + message);
    }

    @RabbitListener(queues=MQConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String message) {
        logger.info("topic queue2 receive message:" + message);
    }

    // 这里的参数类型是byte[]
    @RabbitListener(queues=MQConfig.HEADERS_QUEUE)
    public void receiveHeader(byte[] message) {
        logger.info("header queue2 receive message:" + new String(message));
    }
     */
}
