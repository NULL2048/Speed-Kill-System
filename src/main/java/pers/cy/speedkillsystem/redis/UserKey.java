package pers.cy.speedkillsystem.redis;

/**
 * 用户key
 */
public class UserKey extends BasePrefix{

    /**
     * 设置用户key的前缀
     * @param prefix
     */
    private UserKey(String prefix) {
        super(prefix);
    }

    /**
     * 获得用户id的key的对象，设置的是永不过期。调用这个getById这个方法的时候，就会返回一个已经设置好prefix的KeyPrefix类型对象
     */
    public static UserKey getById = new UserKey("id");

    /**
     * 获得用户name的key的对象，设置的是永不过期
     */
    public static UserKey getByName = new UserKey("name");
}
