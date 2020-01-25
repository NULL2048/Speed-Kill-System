package pers.cy.speedkillsystem.validator;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.spi.time.TimeProvider;
import org.thymeleaf.util.StringUtils;
import pers.cy.speedkillsystem.util.ValidatorUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.metadata.ConstraintDescriptor;
import java.util.List;

/**
 * 手机号的校验器
 */
// 前面的类型是注解类型，后面的类型是要校验的数据类型
public class isMobileValidator implements ConstraintValidator<isMobile, String> {

    private boolean required = false;

    /**
     * 初始化方法  取得注解
     * @param isMobile
     */
    @Override
    public void initialize(isMobile isMobile) {
        required = isMobile.required();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (required) {
            return ValidatorUtil.isMobile(s);
        } else {
            if (StringUtils.isEmpty(s)) {
                return true;
            } else {
                return ValidatorUtil.isMobile(s);
            }
        }
    }
}
