package pers.cy.speedkillsystem.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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

    @Select("select g.*,sg.stock_count,sg.start_date,sg.end_date,sg.sks_price from sks_goods sg left join goods g on sg.goods_id = g.id where g.id = #{goodsId}")
    GoodsVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);
}

