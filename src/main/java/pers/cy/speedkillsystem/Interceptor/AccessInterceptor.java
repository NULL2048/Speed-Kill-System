package pers.cy.speedkillsystem.Interceptor;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import pers.cy.speedkillsystem.annotation.AccessLimit;
import pers.cy.speedkillsystem.config.UserContext;
import pers.cy.speedkillsystem.domain.SksUser;
import pers.cy.speedkillsystem.redis.AccessKey;
import pers.cy.speedkillsystem.redis.RedisService;
import pers.cy.speedkillsystem.result.CodeMsg;
import pers.cy.speedkillsystem.result.Result;
import pers.cy.speedkillsystem.service.SksUserService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private SksUserService sksUserService;

    @Autowired
    private RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            // 从请求中获取用户信息
            SksUser user = getUser(request, response);
            // 将获取的用户信息保存到当前线程中
            UserContext.setUser(user);

            HandlerMethod hm = (HandlerMethod) handler;
            // 获取该请求的AccessLimit标签
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            // 校验该controller是否有AccessLimit标签
            if (accessLimit == null) {
                // 如果没有直接返回true，表示不拦截,请求通过
                return true;
            }

            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            int seconds = accessLimit.seconds();

            // 获取请求路径
            String key = request.getRequestURI();

            // 校验是否需要登陆
            if (needLogin) {
                // 校验是否登录
                if (user == null) {
                    // 将未登录提示输出
                    render(response, CodeMsg.SESSION_ERROR);
                    // 阻止请求
                    return false;
                }
                // 将请求路径与用户拼接作为存入redis的key
                key += "_" + user.getId();
            }

            AccessKey ak = AccessKey.withExpire(seconds);
            // 校验请求次数
            Integer count = redisService.get(ak, "" + key, Integer.class);
            // 如果还没有过请求，将请求次数置为1
            if (count == null) {
                redisService.set(ak, key, 1);
            } else if (count < maxCount) {
                redisService.incr(AccessKey.access, key);
            } else {
                // 访问太频繁
                render(response, CodeMsg.ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return true;
    }

    private void render(HttpServletResponse response, CodeMsg codeMsg) throws IOException {
        // 指定输出格式为JSON的UTF-8字符串
        response.setContentType("application/json;charset=UTF-8");
        // 获取response输出流
        ServletOutputStream outputStream = response.getOutputStream();
        // 将JSON数据换成普通字符串
        String str = JSON.toJSONString(Result.error(codeMsg));
        // 将字符串输出到浏览器
        outputStream.write(str.getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();
    }

    /**
     * 获取根据请求获取用户信息
     * @param request
     * @param response
     * @return
     */
    private SksUser getUser(HttpServletRequest request, HttpServletResponse response) {
        String paramToken = request.getParameter(SksUserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request, SksUserService.COOKIE_NAME_TOKEN);


        // 如果都没有登陆凭证，则跳转回登陆页面
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        // 优先取参数中的token，如果没有再取cookie中的token
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;

        // 根据token取得用户对象
        return sksUserService.getByToken(token, response);
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
