package pers.cy.speedkillsystem.redis;

public abstract class BasePrefix implements KeyPrefix{
    // 有效时长
    private int expireSeconds;
    // 前缀
    private String prefix;

    // 只传入前缀，默认有效时长为永久 这里可以使用public因为抽象类别人是没有办法new的
    public BasePrefix(String prefix) {
        this(0, prefix);
    }

    public BasePrefix(int expireSeconds, String prefix) {
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }

    /**
     * 默认0代表永不过期
     * @return
     */
    @Override
    public int expireSeconds() {
        return expireSeconds;
    }

    @Override
    public String getPrefix() {
        // 获取当前类的类名
        String className = getClass().getSimpleName();
        // 类名拼接上前缀就是最终的前缀值
        return className + ":" + prefix;
    }
}
