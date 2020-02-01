package pers.cy.speedkillsystem.redis;

/**
 * 订单key
 */
public class OrderKey extends BasePrefix{
    // 订单缓存设置为永久不过期
    public OrderKey(String prefix) {
        super(prefix);
    }

    public static OrderKey getSpeedKillOrderByUidGid = new OrderKey("skug");
}
