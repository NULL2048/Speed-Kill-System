package pers.cy.speedkillsystem.redis;

public class GoodsKey extends BasePrefix {
    private GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    // 商品列表key
    public static GoodsKey getGoodsList = new GoodsKey(60, "gl");

    // 商品详情key
    public static GoodsKey getGoodsDetail = new GoodsKey(60, "gd");

    // 商品库存key
    public static GoodsKey getSpeedKillGoodsStock = new GoodsKey(0, "gs");


}
