package pers.cy.speedkillsystem.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pers.cy.speedkillsystem.redis.RedisService;

/**
 * 消息队列发送者/生产者
 */
@Service
public class MQSender {

    private static Logger logger = LoggerFactory.getLogger(MQSender.class);

    // 注入消息队列的操作类
    @Autowired
    private AmqpTemplate amqpTemplate;

    public void send(Object message) {
        // 将消息对象转换成String
        String msg = RedisService.beanToString(message);
        logger.info("send message:" + msg);
        // 将消息发送到指定的消息队列中
        amqpTemplate.convertAndSend(MQConfig.QUEUE, msg);
    }

    public void sendTopic(Object message) {
        // 将消息对象转换成String
        String msg = RedisService.beanToString(message);
        logger.info("send topic message:" + msg);
        // 将消息发送到指定的消息队列中  指定要发送的交换机和匹配字符串，这样交换机能根据匹配字符串来确定要把消息转发给哪一个消息队列
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, "topic.key1", msg + "1");
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, "topic.key2", msg + "2");
    }

    public void sendFanout(Object message) {
        // 将消息对象转换成String
        String msg = RedisService.beanToString(message);
        logger.info("send fanout message:" + msg);
        // 将消息发送到指定的消息队列中  不需要key参数了，但是还是要传参一个""空字符串
        amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE, "", msg);
    }

    public void sendHeader(Object message) {
        // 将消息对象转换成String
        String msg = RedisService.beanToString(message);
        logger.info("send fanout message:" + msg);
        // 设置匹配键值对
        MessageProperties properties = new MessageProperties();
        properties.setHeader("header1", "value1");
        properties.setHeader("header2", "value2");

        // headers模式传给交换机的是Message对象，里面有msg信息的字节数组，还有匹配键值对。只有匹配键值对符合交换机指定的条件消息才会被转发
        Message obj = new Message(msg.getBytes(), properties);
        // 将消息发送到指定的消息队列中  不需要key参数了，但是还是要传参一个""空字符串
        amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE, "", obj);
    }
}
