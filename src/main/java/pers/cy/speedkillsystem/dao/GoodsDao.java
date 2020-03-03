package pers.cy.speedkillsystem.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import pers.cy.speedkillsystem.domain.Goods;
import pers.cy.speedkillsystem.domain.SksGoods;
import pers.cy.speedkillsystem.vo.GoodsVo;

import java.util.List;

@Mapper
public interface GoodsDao {
    
    /**
     * 查询所有秒杀商品的数据
     * @return
     */
    @Select("select g.*,sg.stock_count,sg.start_date,sg.end_date,sg.sks_price from sks_goods sg left join goods g on sg.goods_id = g.id")
    public List<GoodsVo> listGoodVos();

    // 返回值GoodsVo类型对象会根据属性字段名自动给对象的属性进行赋值，但后返回对象
    @Select("select g.*,sg.stock_count,sg.start_date,sg.end_date,sg.sks_price from sks_goods sg left join goods g on sg.goods_id = g.id where g.id = #{goodsId}")
    GoodsVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);

                                                                     // 自动根据属性名从g对象中取出id的值   在这里还加了一条限制and stock_count > 0用来防止超卖，因为数据库每一条SQL执行就是一个线程，数据库不能能出现两个线程同时进行操作的情况，所以在这里加了一个判断条件保证存库大于零，也就避免了两个用户同时请求出现超卖的情况，通过数据库避免了这种情况
    @Update("update sks_goods set stock_count = stock_count - 1 where goods_id = #{goodsId} and stock_count > 0")
    int reduceStock(SksGoods g);
}

