package pers.cy.speedkillsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pers.cy.speedkillsystem.dao.GoodsDao;
import pers.cy.speedkillsystem.domain.OrderInfo;
import pers.cy.speedkillsystem.domain.SksOrder;
import pers.cy.speedkillsystem.domain.SksUser;
import pers.cy.speedkillsystem.redis.RedisService;
import pers.cy.speedkillsystem.redis.SpeedKillKey;
import pers.cy.speedkillsystem.util.MD5Util;
import pers.cy.speedkillsystem.util.UUIDUtil;
import pers.cy.speedkillsystem.vo.GoodsVo;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Service
public class SpeedKillService {
    // 有一个开发习惯要养成，在自己的service中只能调用自己的dao，所以下面这样的写法是有问题的。
    // 所以这里如果我们想使用查询goods的操作，可以调用GoodsService
//    @Autowired
//    private GoodsDao goodsDao;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisService redisService;


    /**
     * 这三个操作应该是一个原子操作，所以要创建事务
     * @param user
     * @param goods
     * @return
     */
    @Transactional
    public OrderInfo speedKill(SksUser user, GoodsVo goods) {
        // 减库存
        boolean flag = goodsService.reduceStock(goods);
        // 减库存成功才生成订单
        if (flag) {
            // 下订单  写入秒杀订单  创建order_info和sks_order  这个操作也应该是一个原子操作，也要添加事务
            return orderService.createOrder(user, goods);
        } else {
            // 如果库存为空，将商品标记为无库存
            setGoodsOver(goods.getId());
            return null;
        }
    }



    /**
     * 获取秒杀结果
     * @param userId
     * @param goodsId
     * @return
     */
    public long getSpeedKillResult(Long userId, long goodsId) {
        SksOrder order = orderService.getSpeedKillOrderByUserIdGoodsId(userId, goodsId);
        if (order != null) {
            return order.getOrderId();
        } else {
            boolean isOver = getGoodsOver(goodsId);
            // 如果已经卖完了，则返回失败
            if (isOver) {
                return -1;
            // 没卖完则返回排队中
            } else {
                return 0;
            }
        }
    }

    /**
     * 标记商品已经卖完
     * @param goodsId
     */
    private void setGoodsOver(Long goodsId) {
        redisService.set(SpeedKillKey.isGoodsOver, "" + goodsId, true);
    }

    /**
     * 查询商品是否卖完
     * @param goodsId
     * @return
     */
    private boolean getGoodsOver(long goodsId) {
        return redisService.exists(SpeedKillKey.isGoodsOver, "" + goodsId);
    }

    public boolean checkPath(SksUser user, long goodsId, String path) {
        if (user == null || path == null) {
            return false;
        }

        String pathOld = redisService.get(SpeedKillKey.getSpeedKillPath, "" + user.getId() + "_" + goodsId, String.class);
        return path.equals(pathOld);
    }

    public String createSpeedKillPath(SksUser user, long goodsId) {
        if (user == null || goodsId <= 0) {
            return null;
        }

        String str = MD5Util.md5(UUIDUtil.uuid() + "1949");
        redisService.set(SpeedKillKey.getSpeedKillPath, "" + user.getId() + "_" + goodsId, str);
        return str;
    }

    /**
     * 生成验证码图片
     * @param user
     * @param goodsId
     * @return
     */
    public BufferedImage createVerifyCode(SksUser user, long goodsId) {
        if (user == null || goodsId <= 0) {
            return null;
        }

        int width = 80;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中
        int rnd = calc(verifyCode);
        redisService.set(SpeedKillKey.getSpeedKillVerifyCode, user.getId() +","+goodsId, rnd);
        //输出图片
        return image;
    }

    /**
     * 根据计算表达式来计算结果
     * @param exp
     * @return
     */
    private int calc(String exp) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer) engine.eval(exp);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    private static char[] ops = new char[]{'+', '-', '*'};
    /**
     * 生成验证码
     * @param rdm
     * @return
     */
    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);

        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];

        // 这里只有""放在最前面才能将后面的这些int和char传换成String，如果放到最后就不能正确的强制转换了
        String exp = "" + num1 + op1 + num2 + op2 + num3;
        return exp;
    }

    /**
     * 校验验证码
     * @param user
     * @param goodsId
     * @param verifyCode
     * @return
     */
    public boolean checkVerifyCode(SksUser user, long goodsId, int verifyCode) {
        if (user == null || goodsId <= 0) {
            return false;
        }

        Integer codeOld = redisService.get(SpeedKillKey.getSpeedKillVerifyCode, user.getId() + "," + goodsId, Integer.class);
        if (codeOld == null || codeOld - verifyCode != 0) {
            return false;
        }

        // 在redis中删除掉这个验证码
        redisService.delete(SpeedKillKey.getSpeedKillVerifyCode, user.getId() + "," + goodsId);
        return true;
    }
}
