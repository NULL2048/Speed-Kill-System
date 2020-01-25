package pers.cy.speedkillsystem.exception;

import com.alibaba.druid.sql.visitor.functions.Bin;
import com.sun.org.apache.bcel.internal.classfile.Code;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import pers.cy.speedkillsystem.result.CodeMsg;
import pers.cy.speedkillsystem.result.Result;

import javax.servlet.http.HttpServletRequest;
import org.springframework.validation.BindException;
import java.util.List;

/**
 * 全局异常拦截器
 * @ControllerAdvice，是Spring3.2提供的新注解,它是一个Controller增强器
 * 可对controller中被@RequestMapping注解的方法加一些逻辑处理。最常用的就是异常处理
 */
@ControllerAdvice
// 可相应返回对象
@ResponseBody
public class GlobalExceptionHandler {
    // 下面这个注解表示拦截哪一类异常，这里的参数是Exception.class，也就是拦截controller抛出了所有异常，这里只是拦截controller抛出的异常，不拦截其他的类抛出的异常
    @ExceptionHandler(value = Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request, Exception e) {
        if (e instanceof GlobalException) {
            GlobalException ex = (GlobalException) e;
            return Result.error(ex.getCm());
        // 检验e是不是spring验证异常（BindException）
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            // 取得异常中的所有错误信息
            List<ObjectError> errors = ex.getAllErrors();
            // 得到第一个错误信息
            ObjectError error = errors.get(0);
            // 获得DefaultMessage
            String msg = error.getDefaultMessage();

            // 返回错误信息
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
        } else {
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}
