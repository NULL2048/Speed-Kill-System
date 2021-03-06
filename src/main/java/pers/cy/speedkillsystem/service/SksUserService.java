package pers.cy.speedkillsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;
import pers.cy.speedkillsystem.dao.SksUserDao;
import pers.cy.speedkillsystem.domain.SksUser;
import pers.cy.speedkillsystem.exception.GlobalException;
import pers.cy.speedkillsystem.redis.RedisService;
import pers.cy.speedkillsystem.redis.SksUserKey;
import pers.cy.speedkillsystem.result.CodeMsg;
import pers.cy.speedkillsystem.result.Result;
import pers.cy.speedkillsystem.util.MD5Util;
import pers.cy.speedkillsystem.util.UUIDUtil;
import pers.cy.speedkillsystem.vo.LoginVo;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class SksUserService {
    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    private SksUserDao sksUserDao;

    @Autowired
    private RedisService redisService;


    /**
     * 通过ID查询用户
     * 这里也做了一个对象缓存，这里只有一级缓存，之前惠农网项目我们做的是二级缓存，还有一个caffein的本地缓存
     * @param id
     * @return
     */
    public SksUser getById(long id) {
        // 取缓存
        SksUser user = redisService.get(SksUserKey.getById, "" + id, SksUser.class);
        if (user != null) {
            return user;
        }
        // 取数据库
        user = sksUserDao.getById(id);
        if (user != null) {
            redisService.set(SksUserKey.getById, "" + id, user);
        }
        return user;
    }

    /**
     * 修改密码
     * 实现对象级缓存
     * @param token
     * @param id
     * @param formPass
     * @return
     */
    public boolean updatePassword(String token, long id, String formPass) {
        // 取user
        SksUser user = getById(id);
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }

        // 更新数据库  要先更新数据库，再更新缓存，这样才能保证缓存和数据库的一致性
        SksUser toBeUpdate = new SksUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass, user.getSalt()));
        // 这里之所以单独创建一个user对象，更新传参的时候相当于只穿两个参数id和password进去，这样在执行sql的时候效率比直接创建一个新的user对象，把所有的属性都更新一遍地sql效率要高，这也是一个提高sql执行效率地小技巧
        sksUserDao.update(toBeUpdate);

        // 更新缓存  修改完db就要也一起更新缓存数据，否则会造成数据库和缓存不一致
        // 将用户信息的缓存删掉
        redisService.delete(SksUserKey.getById, "" + id);
        // 将token中的user数据更新
        user.setPassword(toBeUpdate.getPassword());
        redisService.set(SksUserKey.token, token, user);

        return true;
    }

    /**
     * 通过token找到用户
     * 从redis中查找  这个做的就是对象缓存
     * @param token
     * @return
     */
    public SksUser getByToken(String token, HttpServletResponse response) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        SksUser user = redisService.get(SksUserKey.token, token, SksUser.class);

        // 判空
        if (user != null) {
            // 延长有效期  这样每一次成功校验了凭证之后就可以重新延长有效期，在真实项目中都是这样的原则
            addCookie(user, token, response);
        }

        return user;
    }


    /**
     * 一般在开发中登录模块都是写在用户的service中的，因为是用户要登陆，所以不要单独写一个loginService了
     * 用户登陆操作
     * @param loginVo
     */
    public String login(LoginVo loginVo, HttpServletResponse response) {
        // 判空
        if (loginVo == null) {
            // 向上抛出异常  最终会由自己编写的异常处理器来处理
            throw new GlobalException(CodeMsg.SERVER_ERROR);
            // return CodeMsg.SERVER_ERROR;
        }
        // 获得从客户端传过来的数据
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        // 判断手机号是否存在
        SksUser user = getById(Long.parseLong(mobile));
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
            // return CodeMsg.MOBILE_NOT_EXIST;
        }
        // 验证密码
        String dbPass = user.getPassword();
        String saltDB = user.getSalt();
        // 转换一下客户端换过来的密码，看是否能和数据库中的密码匹配
        String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
        if (!calcPass.equals(dbPass)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
            // return CodeMsg.PASSWORD_ERROR;
        }

        // 生成cookie
        // 用户登录完成之后需要生成一个token登陆凭证发给服务器  这样与本地的cookie相关联。用来标识登陆后用户的身份
        String token = UUIDUtil.uuid();

        // 只要客户端本地已经有了cookie，在cookie的有效时长内，有效路径内的所有向服务器发送的请求request对象都会自动携带者cookie给服务器
        // 这样也就是在登陆以后的所有请求都会携带这cookie中的token来让服务器进行身份校验

        addCookie(user, token, response);

        return token;
    }

    /**
     * 根据传入的token生成cookie，并将其添加到redis中
     * @param user
     * @param response
     */
    private void addCookie(SksUser user, String token, HttpServletResponse response) {
        // 将生成的token存放到第三方缓存数据库中，这样效率是最高的  也就是设置分布式session，不用原生的session了，用redis代替以前session的功能
        // 前两个参数形成真正的key，后一个参数是value
        redisService.set(SksUserKey.token, token, user);
        // 将token存放到cookie中，然后向用户发请求的时候就会通过session将cookie中的内容传入到服务器
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        // 将cookie的有效期和分布式session中的有效期设为一致
        cookie.setMaxAge(SksUserKey.token.expireSeconds());

        // 设置cookie有效路径，/表示项目根路径，即cookie在所有路径下都有效
        cookie.setPath("/");
        // 将cookie添加到response中从服务器响应到客户端，这样客户端也就存有这个cookie了
        response.addCookie(cookie);
    }

}
