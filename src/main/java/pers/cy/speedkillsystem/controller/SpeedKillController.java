package pers.cy.speedkillsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import pers.cy.speedkillsystem.domain.OrderInfo;
import pers.cy.speedkillsystem.domain.SksOrder;
import pers.cy.speedkillsystem.domain.SksUser;
import pers.cy.speedkillsystem.result.CodeMsg;
import pers.cy.speedkillsystem.result.Result;
import pers.cy.speedkillsystem.service.GoodsService;
import pers.cy.speedkillsystem.service.OrderService;
import pers.cy.speedkillsystem.service.SpeedKillService;
import pers.cy.speedkillsystem.vo.GoodsVo;

@Controller
@RequestMapping("/speed_kill")
public class SpeedKillController {
    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService oderService;

    @Autowired
    private SpeedKillService speedKillService;

    /**
     * 秒杀操作
     *
     * GET POST有什么区别？
     * 其实HTTP协议并没有规定GET和POST传输数据量的大小，并不要求POST比GET传输数据量一定多
     * GET 是一种幂等操作，对服务器中的数据不会产生修改的操作就要用GET请求，比如像访问某个页面
     * POST 不是幂等操作，对服务器中的数据有修改的操作一般就用POST请求，比如删除信息，修改信息
     * 如果一个对数据有修改的操作使用GET请求，很明显这是错误的
     *
     *
     * 超卖问题有两种情况：
     * ① 两个人同时发出秒杀请求，如果库存此时还剩下一个，两个人判断的时候库存都不为空，就会导致多卖了一个，所以这个在SQL语句的时候做了一个判断解决了这个问题
     * ② 一个人同时发出了两个秒杀请求，但是规定一个人对同一个商品只能秒杀一次，所以在判断的时候两个秒杀请求都会判定为还没有秒杀过，就会下两次订单。虽然这个问题各异让用户在下订单前输入验证码解决，但是我们还是要从根本上杜绝这个问题。就是在数据库层面对秒杀订单的用户id和商品id做一个唯一索引，这样当数据库中有这一个秒杀订单数据，如果再插入一个相同用户对相同商品的订单是插不进去的，这也就从根本上杜绝了这个问题
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/do_speed_kill", method = RequestMethod.POST)
    @ResponseBody
    public Result<OrderInfo> speedKill(Model model, SksUser user, @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            // 用户session失效
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        // 判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0) {
            // 秒杀结束，没有库存了
            return Result.error(CodeMsg.SPEED_KILL_OVER);
        }

        // 判断用户是否已经秒杀过了，防止用户多次秒杀
        SksOrder order = oderService.getSpeedKillOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            // 不能重复秒杀
            return Result.error(CodeMsg.REPEATE_SPEED_KILL);
        }

        // 减库存  下订单  写入秒杀订单   这三个操作必须是一个事务原子操作，所以都写到speedKillService的方法中
        OrderInfo orderInfo = speedKillService.speedKill(user, goods);

        // 将订单数据返回
        return Result.success(orderInfo);
    }

}
