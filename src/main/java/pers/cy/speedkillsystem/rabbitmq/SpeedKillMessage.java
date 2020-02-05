package pers.cy.speedkillsystem.rabbitmq;

import pers.cy.speedkillsystem.domain.SksUser;

public class SpeedKillMessage {
    private SksUser user;
    private long goodsId;

    public SksUser getUser() {
        return user;
    }

    public void setUser(SksUser user) {
        this.user = user;
    }

    public long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(long goodsId) {
        this.goodsId = goodsId;
    }
}
