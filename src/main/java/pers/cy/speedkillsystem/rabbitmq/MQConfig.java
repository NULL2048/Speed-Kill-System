package pers.cy.speedkillsystem.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置RabbitMQ的bean
 */
@Configuration
public class MQConfig {

    // 指定消息队列的名字
    public static final String SPEED_KILL_QUEUE = "sk.queue";
    public static final String QUEUE = "queue";
    public static final String TOPIC_QUEUE1 = "topic.queue1";
    public static final String TOPIC_QUEUE2 = "topic.queue2";
    public static final String HEADERS_QUEUE = "header.queue2";

    // 交换机名字
    public static final String TOPIC_EXCHANGE = "topicExchange";
    public static final String FANOUT_EXCHANGE = "fanoutExchange";
    public static final String HEADERS_EXCHANGE = "headersExchange";





    /**
     * 将消息队列装载入spring容器
     * RabbitMQ有四种交换机（Exchange）模式，这个模式是最简单的Direct模式,Direct模式使用默认Exchange
     *
     * RabbitMQ发送者发送消息并不是直接发送到队列中，而是先发送到交换机中，由交换机做了一次路由，转发到队列中
     */
    @Bean
    public Queue queue() {
        // true表示是否要做优化
        return new Queue(SPEED_KILL_QUEUE, true);
    }

    /**
     * Topic模式
     */
    @Bean
    public Queue topicQueue1() {
        return new Queue(TOPIC_QUEUE1, true);
    }
    @Bean
    public Queue topicQueue2() {
        return new Queue(TOPIC_QUEUE2, true);
    }
    // 创建Topic模式的Exchange，除了Direct模式，其他三个模式都需要自己定义交换机
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE);
    }
    // 将交换机与消息队列绑定，这样交换机才能将消息转发给指定的消息队列
    @Bean
    public Binding topicBinding1() {
        // 将topicQueue1与topicExchange绑定，匹配字符串是topic.key1
        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with("topic.key1");
    }
    @Bean
    public Binding topicBinding2() {
        // 将topicQueue2与topicExchange绑定，匹配字符串是topic.#，也就是只要是匹配字符串符合这个格式就会被转发给topicQueue2，因为topic.key1也符合这个格式，所以发给topicQueue1的消息也会被转发给这个消息队列topicQueue2
        return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with("topic.#");
    }

    /**
     * Fanout模式   广播模式
     */
    // 创建交换机
    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(FANOUT_EXCHANGE);
    }
    // 将交换机与队列绑定  广播交换机绑定这两个队列，所以这个交换机会将传过来的消息转发给所有它绑定的队列中去
    @Bean
    public Binding fanoutBinding1() {
        // 将topicQueue1与fanoutExchange绑定
        return BindingBuilder.bind(topicQueue1()).to(fanoutExchange());
    }
    @Bean
    public Binding fanoutBinding2() {
        // 将topicQueue1与fanoutExchange绑定
        return BindingBuilder.bind(topicQueue2()).to(fanoutExchange());
    }

    /**
     * Header模式
     */
    // 创建交换机
    @Bean
    public HeadersExchange headersExchange() {
        return new HeadersExchange(HEADERS_EXCHANGE);
    }
    // 创建消息队列
    @Bean
    public Queue headerQueue1() {
        return new Queue(HEADERS_QUEUE, true);
    }
    // 将交换机与消息队列绑定
    @Bean
    public Binding headerBinding() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("header1", "value1");
        map.put("header2", "value2");
        // 将topicQueue1与fanoutExchange绑定  whereAll表示的是发送者的所有的key-value对全部满足上面设定的才会被转发给消息队列  whereAny表示的是只要有一个满足就会被转发
        return BindingBuilder.bind(headerQueue1()).to(headersExchange()).whereAll(map).match();
    }
}
