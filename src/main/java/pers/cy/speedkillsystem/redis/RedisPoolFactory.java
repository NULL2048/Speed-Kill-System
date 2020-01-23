package pers.cy.speedkillsystem.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Service
public class RedisPoolFactory {
    // 注入自己编写的RedisConfig对象 该对象已经被application中的配置信息赋值
    @Autowired
    private RedisConfig redisConfig;

    // 将JedisPool对象装载入spring容器
    @Bean
    public JedisPool JedisPoolFactory() {
        // 创建Jedis配置类
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        // 将自定义redis配置类设定的配置赋值给Jedis配置类
        poolConfig.setMaxIdle(redisConfig.getPoolMaxIdle());
        poolConfig.setMaxTotal(redisConfig.getPoolMaxTotal());
        poolConfig.setMaxWaitMillis(redisConfig.getPoolMaxWait() * 1000); // 传入单位是毫秒，所以要乘1000

        // 创建JedisPool连接池对象
        JedisPool jedisPool = new JedisPool(poolConfig, redisConfig.getHost(), redisConfig.getPort(), redisConfig.getTimeout() * 1000);

        // 返回这个对象表示将这个对象装载入spring容器
        return jedisPool;
    }
}
