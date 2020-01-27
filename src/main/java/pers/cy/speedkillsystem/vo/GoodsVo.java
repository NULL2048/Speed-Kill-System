package pers.cy.speedkillsystem.vo;

import pers.cy.speedkillsystem.domain.Goods;

import java.util.Date;

/**
 * 讲商品表和秒杀商品表合并到一起
 * 这个继承了Goods类，然后又添加了SksGoods类中的三个属性
 */
public class GoodsVo extends Goods {
    private Double sksPrice;
    private Integer stockCount;
    private Date startDate;
    private Date endDate;

    public Double getSksPrice() {
        return sksPrice;
    }

    public void setSksPrice(Double sksPrice) {
        this.sksPrice = sksPrice;
    }

    public Integer getStockCount() {
        return stockCount;
    }

    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
