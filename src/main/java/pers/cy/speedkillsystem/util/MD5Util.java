package pers.cy.speedkillsystem.util;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * 项目中用户密码会经过两次md5再进行入库
 * 第一次是从客户端将用户输入的密码拼接固定的salt进行md5加密，这样在客户端向服务器过程中数据就可以被加密，第一次使用的salt必须是固定的，否则服务器收到数据库没有办法进行解析
 * 第二次是在服务器将接收到的密码数据拼接一个随机的salt进行md5加密，并将其存入到数据库中
 */
public class MD5Util {
    // 进行md5加密
    public static String md5(String src) {
        return DigestUtils.md5Hex(src);
    }

    // 客户端固定salt
    private static final String SALT = "1a2b3c4d";

    /**
     * 将用户输入的密码进行md5加密
     * @param inputPass
     * @return
     */
    public static String inputPassToFormPass(String inputPass) {
        // 将用户输入密码与客户端固定salt拼接
        String str = "" + SALT.charAt(0) + SALT.charAt(2) + inputPass + SALT.charAt(5) + SALT.charAt(4);
        return md5(str);
    }

    /**
     * 将服务器接收的数据进行md5加密
     * @param formPass
     * @param salt
     * @return
     */
    public static String formPassToDBPass(String formPass, String salt) {
        // 将服务器接收的数据与随机salt进行拼接
        String str = ""+salt.charAt(0)+salt.charAt(2) + formPass +salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    /**
     * 将用户输入密码转换成存入数据库密码
     * @param inputPass
     * @param saltDB
     * @return
     */
    public static String inputPassToDbPass(String inputPass, String saltDB) {
        String formPass = inputPassToFormPass(inputPass);
        String dbPass = formPassToDBPass(formPass, saltDB);
        return dbPass;
    }

    public static void main(String[] args) {
        System.out.println(inputPassToFormPass("123456"));//d3b1294a61a07da9b49b6e22b2cbd7f9
    }
}
