package pers.cy.speedkillsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import pers.cy.speedkillsystem.domain.OrderInfo;
import pers.cy.speedkillsystem.domain.SksUser;
import pers.cy.speedkillsystem.redis.RedisService;
import pers.cy.speedkillsystem.result.CodeMsg;
import pers.cy.speedkillsystem.result.Result;
import pers.cy.speedkillsystem.service.GoodsService;
import pers.cy.speedkillsystem.service.OrderService;
import pers.cy.speedkillsystem.service.SksUserService;
import pers.cy.speedkillsystem.vo.GoodsVo;
import pers.cy.speedkillsystem.vo.OrderDetailVo;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private SksUserService sksUserService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private GoodsService goodsService;

    /**
     * 获取用户信息
     * @param model
     * @param user
     * @return
     */
    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model, SksUser user, @RequestParam("orderId") long orderId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        // 获取订单信息
        OrderInfo order = orderService.getOrderById(orderId);
        if (order == null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        // 获取商品信息
        long goodsId = order.getGoodsId();
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        // 返回订单详情对象
        OrderDetailVo vo = new OrderDetailVo();
        vo.setOrder(order);
        vo.setGoods(goods);
        return Result.success(vo);
    }
}
