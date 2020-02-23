package pers.cy.speedkillsystem.controller;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pers.cy.speedkillsystem.domain.OrderInfo;
import pers.cy.speedkillsystem.domain.SksOrder;
import pers.cy.speedkillsystem.domain.SksUser;
import pers.cy.speedkillsystem.rabbitmq.MQSender;
import pers.cy.speedkillsystem.rabbitmq.SpeedKillMessage;
import pers.cy.speedkillsystem.redis.GoodsKey;
import pers.cy.speedkillsystem.redis.RedisService;
import pers.cy.speedkillsystem.redis.SpeedKillKey;
import pers.cy.speedkillsystem.result.CodeMsg;
import pers.cy.speedkillsystem.result.Result;
import pers.cy.speedkillsystem.service.GoodsService;
import pers.cy.speedkillsystem.service.OrderService;
import pers.cy.speedkillsystem.service.SpeedKillService;
import pers.cy.speedkillsystem.util.MD5Util;
import pers.cy.speedkillsystem.util.UUIDUtil;
import pers.cy.speedkillsystem.vo.GoodsVo;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/speed_kill")
public class SpeedKillController implements InitializingBean {
    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService oderService;

    @Autowired
    private SpeedKillService speedKillService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MQSender sender;

    // 商品售空标记
    private Map<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();

    /**
     * 实现InitializingBean接口之后，在系统初始化会先自动调用这个方法
     * 在系统初始化的时候先将库存预加载入redis缓存
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if (goodsList == null) {
            return;
        }
        // 将商品库存加载入缓存
        for (GoodsVo goods : goodsList) {
            redisService.set(GoodsKey.getSpeedKillGoodsStock, "" + goods.getId(), goods.getStockCount());
            // 初始化阶段先将所有商品标记为没有售空
            localOverMap.put(goods.getId(), false);
        }
    }

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
    @RequestMapping(value = "/{path}/do_speed_kill", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> speedKill(Model model, SksUser user, @RequestParam("goodsId") long goodsId,
        @PathVariable("path") String path) {
        model.addAttribute("user", user);
        if (user == null) {
            // 用户session失效
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        // 验证path
        boolean check = speedKillService.checkPath(user, goodsId, path);
        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        // 先查询该商品是否已经无库存，如果无库存直接返回失败，就不要再预减库存了
        // 如果不做这一步判断，那么预减库存有可能变成负数，当变为负数后这么一直访问redis是没有意义的，后面也肯定创建不了订单，还会白白占用redis资源，有网络消耗
        // 所以使用hashMap直接在本地内存存储该商品是否已经售空的标识来避免这一问题
        boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.SPEED_KILL_OVER);
        }

        // 预减库存
        Long stock = redisService.decr(GoodsKey.getSpeedKillGoodsStock, "" + goodsId);
        // 如果库存已经小于零直接返回失败
        if (stock < 0) {
            // 如果该商品的库存已经没有了，将该商品id标记为已经无库存
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.SPEED_KILL_OVER);
        }

        // 判断用户是否已经秒杀过了，防止用户多次秒杀
        SksOrder order = oderService.getSpeedKillOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            // 不能重复秒杀
            return Result.error(CodeMsg.REPEATE_SPEED_KILL);
        }

        // 入队,异步下单  同时客户端进行轮询
        // 创建订单消息对象
        SpeedKillMessage skMsg = new SpeedKillMessage();
        skMsg.setUser(user);
        skMsg.setGoodsId(goodsId);
        sender.sendSpeedKillMessage(skMsg);

        // 返回0，表示排队中
        return Result.success(0);

        //
        /*
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
         */
    }

    /**
     * 查询订单是否创建成功
     * @param model
     * @param user
     * @param goodsId
     * @return  返回orderId表示下单成功  返回-1表示库存不足，下单失败   0：排队中
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> speedKillResult(Model model, SksUser user, @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            // 用户session失效
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        long result = speedKillService.getSpeedKillResult(user.getId(), goodsId);
        return Result.success(result);
    }

    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getSpeedKillPath(Model model, SksUser user, @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            // 用户session失效
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        String path = speedKillService.createSpeedKillPath(user, goodsId);
        return Result.success(path);
    }

    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getSpeedKillVerifyCode(HttpServletResponse response, SksUser user, @RequestParam("goodsId") long goodsId) {
        if (user == null) {
            // 用户session失效
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        BufferedImage image = speedKillService.createVerifyCode(user, goodsId);
        try {
            ServletOutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.SPEED_KILL_FALL);
        }
    }
}
