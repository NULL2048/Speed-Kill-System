package pers.cy.speedkillsystem.util;

import java.util.UUID;

public class UUIDUtil {
    public static String uuid() {
        // 返回一个随机的字符串   去掉了-
        return UUID.randomUUID().toString().replace("-", "");
    }
}
