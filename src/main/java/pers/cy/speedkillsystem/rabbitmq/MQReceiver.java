package pers.cy.speedkillsystem.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * 消息队列接收者/消费者
 */
@Service
public class MQReceiver {

    private static Logger logger = LoggerFactory.getLogger(MQReceiver.class);

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
}
