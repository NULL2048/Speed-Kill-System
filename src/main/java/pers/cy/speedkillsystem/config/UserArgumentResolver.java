package pers.cy.speedkillsystem.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import pers.cy.speedkillsystem.domain.SksUser;
import pers.cy.speedkillsystem.service.SksUserService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 对controller中方法的参数中的SksUser类型进行赋值的类
 * 这个东西就叫做参数解析器
 */
// 因为里面注入了一个SksUserService的bean，所以这个类也得让spring管理，所以才加的这个注解
@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    @Autowired
    private SksUserService sksUserService;

    /**
     * 通过这个方法获取controller方法中的参数类型
     * 返回true才会进一步进行后面的resolveArgument方法处理
     * @param methodParameter
     * @return
     */
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        // 获取参数类型
        Class<?> clazz = methodParameter.getParameterType();
        // 如果是用户类型返回true
        return clazz == SksUser.class;
    }

    /**
     * 如果controller方法得参数中有Sksuser这个的对象需要赋值，就会启动这个参数解析器，下面是我们编写的解析方法
     * 他会自动从cookie或者request请求的参数中取得token,然后通过token取得SksUser对象返回，这样就实现了参数自动赋值
     * 这样以后如果我们从redis中获取分布式session的方式改变了，只需要改这一个方法就行了，不用在每一个使用到session的地方都修改
     * @param methodParameter
     * @param modelAndViewContainer
     * @param nativeWebRequest
     * @param webDataBinderFactory
     * @return
     * @throws Exception
     */
    @Override
   public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        // 在拦截器已经将用户信息存到当前线程了，以后每次需要用户信息直接从本次请求的当前线程取就行了
        return UserContext.getUser();
    }

    /**
     * 遍历cookie，取得cookie的内容
     * @param request
     * @param cookieName
     * @return
     */
    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();

        // 判空，一定要养成良好的习惯
        if (cookies == null || cookies.length <= 0) {
            return null;
        }

        // 遍历所有的cookie，取得指定的cookie值
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
