package pers.cy.speedkillsystem.result;

import com.sun.org.apache.bcel.internal.classfile.Code;

/**
 * 这个类是用来封装操作响应数据的
 */
public class CodeMsg {
    private int code;
    private String msg;

    // 这种写法可以方便统一管理异常代码和异常信息
    // 通用异常
    public static CodeMsg SUCCESS = new CodeMsg(0, "success");
    public static CodeMsg SERVER_ERROR = new CodeMsg(500100, "服务器异常");
    public static CodeMsg BIND_ERROR = new CodeMsg(500101, "参数校验异常：%s");

    // 登录模块 5002XX
    public static CodeMsg SESSION_ERROR = new CodeMsg(500210, "Session不存在或者已经失效");
    public static CodeMsg PASSWORD_EMPTY = new CodeMsg(500211, "密码不能为空");
    public static CodeMsg MOBILE_EMPTY = new CodeMsg(500212, "手机号不能为空");
    public static CodeMsg MOBILE_ERROR = new CodeMsg(500213, "手机号格式错误");
    public static CodeMsg MOBILE_NOT_EXIST = new CodeMsg(500214, "手机号不存在");
    public static CodeMsg PASSWORD_ERROR = new CodeMsg(500215, "密码错误");



    // 商品模块 5003XX

    // 订单模块 5004XX

    // 秒杀模块 5005XX
    public static CodeMsg SPEED_KILL_OVER = new CodeMsg(500500, "商品已经秒杀完毕");
    public static CodeMsg REPEATE_SPEED_KILL = new CodeMsg(500501, "不能重复秒杀");




    private CodeMsg(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }


    public String getMsg() {
        return msg;
    }

    // 参数是变参 即可以写任意多个参数
    public CodeMsg fillArgs(Object... args) {
        int code = this.code;
        // 根据msg的指定格式，讲args的内容格式化输出成String  这个就可以用在BIND_ERROR这个上面
        // String.format是字符串格式化输出
        String message = String.format(this.msg, args);
        return new CodeMsg(code, message);
    }

    @Override
    public String toString() {
        return "CodeMsg [code=" + code + ", msg=" + msg + "]";
    }
}
