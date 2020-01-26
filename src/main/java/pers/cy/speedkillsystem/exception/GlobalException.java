package pers.cy.speedkillsystem.exception;

import pers.cy.speedkillsystem.result.CodeMsg;

/**
 * 全局异常
 * 所有的异常向上层抛出的时候都要使用这个类，这样才可以夹带自己设定的错误信息，也方便统一管理异常
 */
public class GlobalException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    private CodeMsg cm;

    public GlobalException(CodeMsg cm) {
        super(cm.toString());
        this.cm = cm;
    }

    public CodeMsg getCm() {
        return cm;
    }

}
