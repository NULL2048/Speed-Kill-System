package pers.cy.speedkillsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pers.cy.speedkillsystem.dao.GoodsDao;
import pers.cy.speedkillsystem.domain.Goods;
import pers.cy.speedkillsystem.domain.SksGoods;
import pers.cy.speedkillsystem.vo.GoodsVo;

import java.util.List;

@Service
public class GoodsService {
    @Autowired
    private GoodsDao goodsDao;

    /**
     * 取得所有商品信息
     * @return
     */
    public List<GoodsVo> listGoodsVo() {
        return goodsDao.listGoodVos();
    }

    /**
     * 根据商品ID得到商品信息
     * @param goodsId
     * @return
     */
    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsDao.getGoodsVoByGoodsId(goodsId);
    }

    /**
     * 减少商品库存
     * @param goods
     */
    public void reduceStock(GoodsVo goods) {
        SksGoods g = new SksGoods();
        g.setGoodsId(goods.getId());
        goodsDao.reduceStock(g);
    }
}
