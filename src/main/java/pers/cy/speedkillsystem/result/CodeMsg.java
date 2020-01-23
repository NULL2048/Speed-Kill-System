package pers.cy.speedkillsystem.result;

import com.sun.org.apache.bcel.internal.classfile.Code;

public class CodeMsg {
    private int code;
    private String msg;

    // 这种写法可以方便统一管理异常代码和异常信息
    // 通用异常
    public static CodeMsg SUCCESS = new CodeMsg(0, "success");
    public static CodeMsg SERVER_ERROR = new CodeMsg(500100, "服务器异常");
    // 登录模块 5002XX

    // 商品模块 5003XX

    // 订单模块 5004XX

    // 秒杀模块 5005XX


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
}
