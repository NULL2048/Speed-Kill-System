package pers.cy.speedkillsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.util.StringUtils;
import pers.cy.speedkillsystem.domain.SksUser;
import pers.cy.speedkillsystem.domain.User;
import pers.cy.speedkillsystem.service.SksUserService;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private SksUserService sksUserService;


    @RequestMapping("/to_list")
    public String list(Model model ,  // 取得名为COOKIE_NAME_TOKEN的cookie对象赋值CookieToken对象   required = false表示这个参数有可能没有，因为这里是要接收电脑端和手机端的请求
//                       @CookieValue(value =SksUserService.COOKIE_NAME_TOKEN, required = false) String cookieToken,
//                         // 为了兼容手机端，有的时候手机端的cookie中的token是不会跟着cookie发到服务器的，而是直接通过request对象中的参数发过来，所以这里也写一下接收request对象中名为COOKIE_NAME_TOKEN的参数
//                         @RequestParam(value = SksUserService.COOKIE_NAME_TOKEN, required = false) String paramToken,
//                         HttpServletResponse response,
                       SksUser user) {

//        // 如果都没有登陆凭证，则跳转回登陆页面
//        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
//            return "login";
//        }
//        // 优先取参数中的token，如果没有再取cookie中的token
//        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
//
//        // 根据token取得用户对象
//        SksUser user = sksUserService.getByToken(token, response);
        model.addAttribute("user", user);
        return "goods_list";
    }


    @RequestMapping("/to_detail")              // 取得名为COOKIE_NAME_TOKEN的cookie对象赋值CookieToken对象   required = false表示这个参数有可能没有，因为这里是要接收电脑端和手机端的请求
    public String detail(Model model , @CookieValue(value =SksUserService.COOKIE_NAME_TOKEN, required = false) String cookieToken,
                         // 为了兼容手机端，有的时候手机端的cookie中的token是不会跟着cookie发到服务器的，而是直接通过request对象中的参数发过来，所以这里也写一下接收request对象中名为COOKIE_NAME_TOKEN的参数
                         @RequestParam(value = SksUserService.COOKIE_NAME_TOKEN, required = false) String paramToken,
                         HttpServletResponse response) {


        return null;
    }
}
