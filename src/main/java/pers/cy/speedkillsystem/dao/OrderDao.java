package pers.cy.speedkillsystem.dao;

import org.apache.ibatis.annotations.*;
import pers.cy.speedkillsystem.domain.OrderInfo;
import pers.cy.speedkillsystem.domain.SksOrder;

@Mapper
public interface OrderDao {
    /**
     * 根据用户ID和商品ID获得秒杀订单
     * @param userId
     * @param goodsId
     * @return
     */
    @Select("select * from sks_order where user_id = #{userId} and goods_id = #{goodsId}")
    SksOrder getSpeedKillOrderByUserIdGoodsId(@Param("userId") long userId, @Param("goodsId") long goodsId);

    // 插入之后还会返回订单ID
    @Insert("insert into order_info (user_id, goods_id, goods_name, goods_count, goods_price, order_channel, status, create_date)values("
            + "#{userId}, #{goodsId}, #{goodsName}, #{goodsCount}, #{goodsPrice}, #{orderChannel},#{status},#{createDate} )")
    // keyColumn表示数据库的列   keyProperty表示domain对象中和数据库列对应的对象中的属性名 resultType表示返回值类型 before表示是否要返回值  statement指定要返回的内容，select last_insert_id()表示返回最后一次插入的id值
    @SelectKey(keyColumn="id", keyProperty="id", resultType=long.class, before=false, statement="select last_insert_id()")
    long insertOrder(OrderInfo orderInfo);

    @Insert("insert into sks_order (user_id, goods_id, order_id)values(#{userId}, #{goodsId}, #{orderId})")
    int insertSksOrder(SksOrder sksOrder);
}
