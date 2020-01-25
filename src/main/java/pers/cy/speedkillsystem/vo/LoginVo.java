package pers.cy.speedkillsystem.vo;

import org.hibernate.validator.constraints.Length;
import pers.cy.speedkillsystem.validator.isMobile;

import javax.validation.constraints.NotNull;

/**
 * 用来保存客户端发给服务器的登陆凭证
 */
public class LoginVo {
    // 以前都需要在每一个使用到loginVo的代码中写上校验代码，十分的繁琐，所以这里引入了校验注解
    // 加了相应注解的属性会自动进行相关的校验
    @NotNull
    @isMobile
    private String mobile;

    // 不能为空
    @NotNull
    // 长度的规范
    @Length(min=32)
    private String password;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginVo{" +
                "mobile='" + mobile + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
