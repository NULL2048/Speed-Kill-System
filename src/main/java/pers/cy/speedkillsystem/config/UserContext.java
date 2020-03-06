package pers.cy.speedkillsystem.config;

import pers.cy.speedkillsystem.domain.SksUser;

public class UserContext {
    /**
     * ThreadLocal是与当前线程绑定的一个数据接口，存储当前线程独享的数据，和其他线程不会产生冲突，线程安全
     * 我们将在拦截器拦截的每次请求取得的用户信息绑定到当前线程中，供该用户在未来的一系列请求中可以直接从ThreadLocal中取得用户信息，就不需要再单独获取用户信息了
     */
    private static ThreadLocal<SksUser> userHolder = new ThreadLocal<SksUser>();

    public static void setUser(SksUser user) {
        userHolder.set(user);
    }

    public static SksUser getUser() {
        return userHolder.get();
    }
}
