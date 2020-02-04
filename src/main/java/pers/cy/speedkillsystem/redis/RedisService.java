package pers.cy.speedkillsystem.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Service
public class RedisService {
    // 注入存放在spring容器中的JedisPool对象
    @Autowired
    private JedisPool jedisPool;

    /**
     * 通过指定的key取得redis中的数据，并将其按照指定的类型返回
     * @param key 键值
     * @param clazz 数据要转化成的指定类型
     * @param <T> 泛型类型
     * @return 从redis中取得的数据
     */
    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz) {
        // 创建jedis对象，准备从连接池中获取该对象，用来操作redis
        Jedis jedis = null;
        // 使用连接池在使用完要将其释放
        try {
            // 通过连接池获得jedis对象
            jedis = jedisPool.getResource();
            // 生成真正的key
            String realKey = prefix.getPrefix() + key;
            // jedis这个对象的get方法返回的都是String类型，先通过key获取数据
            String str = jedis.get(realKey);
            // 将获取到的String数据转换成指定的类型对象
            T t = stringToBean(str, clazz);
            // 返回该对象
            return t;
        } finally {
            // 释放资源
            returnToPool(jedis);
        }
    }

    /**
     * 向redis中添加键值对
     * @param key 要添加的键
     * @param value 要添加的值
     * @param <T> 值的类型
     * @return true/false
     */
    public <T> boolean set(KeyPrefix prefix, String key, T value) {
        // 创建jedis对象，准备从连接池中获取该对象，用来操作redis
        Jedis jedis = null;
        // 使用连接池在使用完要将其释放
        try {
            // 通过连接池获得jedis对象
            jedis = jedisPool.getResource();
            // 将要存入的值转换成String形式，因为jedis的set方法要传入的值规定必须是String
            String str = beanToString(value);
            // 对str进行非法判断
            if (str == null || str.length() <= 0) {
                return false;
            }
            // 生成真正的key
            String realKey = prefix.getPrefix() + key;
            // 获取用户设定的有效时长
            int seconds = prefix.expireSeconds();
            // 永不过期
            if (seconds <= 0) {
                // 将数据放入redis
                jedis.set(realKey, str);
            // 其他情况要设置有效时长
            } else {
                // setex方法创建有有效期的键值对
                jedis.setex(realKey, seconds, str);
            }
            return true;
        } finally {
            // 释放
            returnToPool(jedis);
        }
    }

    /**
     * 判断一个key存不存在
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> boolean exists(KeyPrefix prefix, String key) {
        // 创建jedis对象，准备从连接池中获取该对象，用来操作redis
        Jedis jedis = null;
        // 使用连接池在使用完要将其释放
        try {
            // 通过连接池获得jedis对象
            jedis = jedisPool.getResource();
            // 生成真正的key
            String realKey = prefix.getPrefix() + key;
            return jedis.exists(realKey);
        } finally {
            // 释放资源
            returnToPool(jedis);
        }
    }

    /**
     * 删除字段
     * @param prefix
     * @param key
     * @return
     */
    public boolean delete(KeyPrefix prefix, String key) {
        // 创建jedis对象，准备从连接池中获取该对象，用来操作redis
        Jedis jedis = null;
        // 使用连接池在使用完要将其释放
        try {
            // 通过连接池获得jedis对象
            jedis = jedisPool.getResource();
            // 生成真正的key
            String realKey = prefix.getPrefix() + key;
            long ret = jedis.del(realKey);
            return ret > 0;
        } finally {
            // 释放资源
            returnToPool(jedis);
        }
    }

    /**
     * 将指定key的值增加1
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> Long incr(KeyPrefix prefix, String key) {
        // 创建jedis对象，准备从连接池中获取该对象，用来操作redis
        Jedis jedis = null;
        // 使用连接池在使用完要将其释放
        try {
            // 通过连接池获得jedis对象
            jedis = jedisPool.getResource();
            // 生成真正的key  将该类型默认的key和用户自己设定的key拼接起来
            String realKey = prefix.getPrefix() + key;
            return jedis.incr(realKey);
        } finally {
            // 释放资源
            returnToPool(jedis);
        }
    }

    /**
     * 将指定key的值减少1
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> Long decr(KeyPrefix prefix, String key) {
        // 创建jedis对象，准备从连接池中获取该对象，用来操作redis
        Jedis jedis = null;
        // 使用连接池在使用完要将其释放
        try {
            // 通过连接池获得jedis对象
            jedis = jedisPool.getResource();
            // 生成真正的key
            String realKey = prefix.getPrefix() + key;
            return jedis.decr(realKey);
        } finally {
            // 释放资源
            returnToPool(jedis);
        }
    }

    /**
     * 将对象转换成String类型
     * @param value 要转换的对象
     * @param <T> 要转换的对象的类型
     * @return 转换完成的String对象
     */
    public static <T> String beanToString(T value) {
        // 对值判空
        if (value == null) {
            return null;
        }

        // 通过反射获得要转换数据的类型    类对象
        Class<?> clazz = value.getClass();
        // 将下面的三种类型全都转换成字符串然后返回
        if (clazz == int.class || clazz == Integer.class) {
            return "" + value;
        } else if (clazz == String.class) {
            return (String) value;
        } else if (clazz == long.class || clazz == Long.class) {
            return "" + value;
        } else {
            // 其他类型使用fastjson转换成JSON字符串返回
            return JSON.toJSONString(value);
        }
    }

    /**
     * 将字符串转换成指定类型的对象
     * @param str 要转换的字符串
     * @param clazz 要转换成的类型
     * @param <T> 要转换成的类型
     * @return 转换完成的对象
     */
    public static <T> T stringToBean(String str, Class<T> clazz) {
        // 数据非法判断
        if (str == null || str.length() <= 0 || clazz == null) {
            return null;
        }

        // 如果指定的转换类型是以下三种类型，就将其转换相应的类型然后返回
        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(str);
        } else if (clazz == String.class) {
            return (T) str;
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(str);
        } else {
            // 指定的其他类型就用fastjson转化为Java对象进行返回
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }

    // 释放jedis
    private void returnToPool(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }
}
