package pers.cy.speedkillsystem.redis;

/**
 * 订单key
 */
public class OrderKey extends BasePrefix{
    public OrderKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
}
