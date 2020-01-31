package pers.cy.speedkillsystem.controller;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.util.StringUtils;
import pers.cy.speedkillsystem.domain.SksUser;
import pers.cy.speedkillsystem.domain.User;
import pers.cy.speedkillsystem.redis.GoodsKey;
import pers.cy.speedkillsystem.redis.RedisService;
import pers.cy.speedkillsystem.service.GoodsService;
import pers.cy.speedkillsystem.service.SksUserService;
import pers.cy.speedkillsystem.vo.GoodsVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
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

    // 使用thymeleaf都会在spring容器中装载ThymeleafViewResolver这个对象，这个对象可以用来渲染界面模板
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    private ApplicationContext applicationContext;


    /**
     * 获得商品列表
     * 对这个页面实现页面缓存
     * 将这个controller的返回值不再是String，直接返回一个html对象
     * @param model
     * @param user
     * @return
     */
    @RequestMapping(value = "/to_list", produces = "text/html")
    @ResponseBody
    public String list(Model model ,  // 取得名为COOKIE_NAME_TOKEN的cookie对象赋值CookieToken对象   required = false表示这个参数有可能没有，因为这里是要接收电脑端和手机端的请求
//                       @CookieValue(value =SksUserService.COOKIE_NAME_TOKEN, required = false) String cookieToken,
//                         // 为了兼容手机端，有的时候手机端的cookie中的token是不会跟着cookie发到服务器的，而是直接通过request对象中的参数发过来，所以这里也写一下接收request对象中名为COOKIE_NAME_TOKEN的参数
//                         @RequestParam(value = SksUserService.COOKIE_NAME_TOKEN, required = false) String paramToken,
//                         HttpServletResponse response,
                       SksUser user, HttpServletRequest request, HttpServletResponse response) {

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

        // 取缓存  如果能在缓存中取到界面就直接返回，如果取不到就直接手动渲染。这个是为了防止瞬间有大量的用户访问才设置的缓存，缓存的有效期不宜过大不宜过小，这里设置的是60秒。太小就会失去缓存左右，太长了用户获得的界面就不是最新的
        // 这里的第二个参数空就行，只需要第一个参数标识就够了
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }

        // 查询商品列表
        List<GoodsVo> goodsVoList = goodsService.listGoodsVo();
        model.addAttribute("goodsList", goodsVoList);

        // return "goods_list";

        SpringWebContext ctx = new SpringWebContext(request, response, request.getServletContext(), request.getLocale(),
            model.asMap(), applicationContext);
        // 手动渲染   在之前都是spring boot帮我们渲染，我们直接返回模板名称就行了
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
        // 如果界面不为空，先将其存入到redis中，方便下次直接获取
        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsList, "", html);
        }
        return html;
    }

    /**
     * 获取商品的详情页
     * 这里做的事URL缓存
     * URL缓存和页面缓存实现都是一样的，唯一不一样的就是URL缓存时有参数的，就是说每一个商品都有一个自己的商品页，做缓存的时候要对每一个商品的页面都做一次缓存
     * 而页面缓存只有一个页面，所有的用户访问的都是一样的，所以页面缓存的优化力度更大。
     * @param model
     * @param user
     * @param goodsId
     * @param response
     * @param request
     * @return
     */
    @RequestMapping(value = "/to_detail/{goodsId}", produces = "text/html")
    @ResponseBody
    public String detail(Model model, SksUser user, @PathVariable("goodsId") long goodsId,
        HttpServletResponse response, HttpServletRequest request){
        model.addAttribute("user", user);

        // 取缓存
        String html = redisService.get(GoodsKey.getGoodsDetail, "" + goodsId, String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }


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

        // return "goods_detail";

        SpringWebContext ctx = new SpringWebContext(request, response, request.getServletContext(), request.getLocale(),
                model.asMap(), applicationContext);
        // 手动渲染
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsDetail, "" + goodsId, html);
        }
        return html;
    }
}
