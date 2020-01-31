package pers.cy.speedkillsystem.vo;

import pers.cy.speedkillsystem.domain.Goods;
import pers.cy.speedkillsystem.domain.SksUser;

public class GoodsDetailVo {
    // 秒杀状态
    private int speedKillStatus = 0;
    // 还剩多少秒秒杀开始
    private int remainSeconds = 0;
    // 商品
    private GoodsVo goods;
    // 用户
    private SksUser user;

    public SksUser getUser() {
        return user;
    }

    public void setUser(SksUser user) {
        this.user = user;
    }

    public int getSpeedKillStatus() {
        return speedKillStatus;
    }

    public void setSpeedKillStatus(int speedKillStatus) {
        this.speedKillStatus = speedKillStatus;
    }

    public int getRemainSeconds() {
        return remainSeconds;
    }

    public void setRemainSeconds(int remainSeconds) {
        this.remainSeconds = remainSeconds;
    }

    public GoodsVo getGoods() {
        return goods;
    }

    public void setGoods(GoodsVo goods) {
        this.goods = goods;
    }
}
