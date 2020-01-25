package pers.cy.speedkillsystem.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 手机号校验注解
 */
// 下面的注解是固定的，都要这样写
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        // 指定手机号校验器
        validatedBy = {isMobileValidator.class}
)
public @interface isMobile {
    // 是否可以为空  true->可以为空
    boolean required() default true;
    // 如果校验不通过的话显示这个错误信息
    String message() default "手机号码格式错误";

    // 下面两个是固定的
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
