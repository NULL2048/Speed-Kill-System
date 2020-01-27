package pers.cy.speedkillsystem.controller;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.util.StringUtils;
import pers.cy.speedkillsystem.domain.SksUser;
import pers.cy.speedkillsystem.domain.User;
import pers.cy.speedkillsystem.redis.RedisService;
import pers.cy.speedkillsystem.service.GoodsService;
import pers.cy.speedkillsystem.service.SksUserService;
import pers.cy.speedkillsystem.vo.GoodsVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private SksUserService sksUserService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private GoodsService goodsService;


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
        // 查询商品列表
        List<GoodsVo> goodsVoList = goodsService.listGoodsVo();
        model.addAttribute("goodsList", goodsVoList);

        return "goods_list";
    }


    @RequestMapping("/to_detail/{goodsId}")
    public String detail(Model model, SksUser user, @PathVariable("goodsId") long goodsId){
        model.addAttribute("user", user);

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goods);

        // 获取秒杀时间  单位毫秒
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        // 秒杀状态
        int speedKillStatus = 0;
        // 还剩多少秒秒杀开始
        int remainSeconds = 0;

        // 秒杀还未开始，倒计时
        if (now < startAt) {
            speedKillStatus = 0;
            remainSeconds = (int) (startAt - now) / 1000;
        // 秒杀已经结束
        } else if (now > endAt) {
            speedKillStatus = 2;
            remainSeconds = -1;
        // 秒杀进行中
        } else {
            speedKillStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("speedKillStatus", speedKillStatus);
        model.addAttribute("remainSeconds", remainSeconds);

        return "goods_detail";
    }
}
