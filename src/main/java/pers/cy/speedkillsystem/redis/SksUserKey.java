package pers.cy.speedkillsystem.redis;

/**
 * 用户key
 */
public class SksUserKey extends BasePrefix{

    // 默认有效时长   两天 单位秒
    private static final int TOKEN_EXPIRE = 3600 * 24 * 2;
    /**
     * 设置用户key的前缀
     * @param prefix
     */
    private SksUserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    /**
     * 获得用户id的key的对象，设置的是永不过期。调用这个getById这个方法的时候，就会返回一个已经设置好prefix的KeyPrefix类型对象
     */
    public static SksUserKey token = new SksUserKey(TOKEN_EXPIRE, "tk");
}
