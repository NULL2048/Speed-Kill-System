package pers.cy.speedkillsystem.util;

import org.apache.tomcat.websocket.WsRemoteEndpointImplClient;
import org.thymeleaf.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 利用正则表达式判断是否符合格式
 */
public class ValidatorUtil {
    // 手机号格式  开头的1就是表示手机号的第一个数字只能是1 然后后面的d{10}表示1后面应该是十个数字
    private static final Pattern mobile_pattern = Pattern.compile("1\\d{10}");

    /**
     * 校验是否是手机号
     * @param src
     * @return
     */
    public static boolean isMobile(String src) {
        // 判空
        if (StringUtils.isEmpty(src)) {
            return false;
        }
        // 判断是否满足格式，详见正则表达式用法
        Matcher matcher = mobile_pattern.matcher(src);
        return matcher.matches();
    }

//    public static void main(String[] args) {
//        System.out.println(isMobile("13323454553"));
//        System.out.println(isMobile("13323453"));
//
//    }
}
