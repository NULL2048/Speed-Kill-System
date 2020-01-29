package pers.cy.speedkillsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import pers.cy.speedkillsystem.domain.SksUser;
import pers.cy.speedkillsystem.redis.RedisService;
import pers.cy.speedkillsystem.result.Result;
import pers.cy.speedkillsystem.service.GoodsService;
import pers.cy.speedkillsystem.service.SksUserService;
import pers.cy.speedkillsystem.vo.GoodsVo;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private SksUserService sksUserService;

    @Autowired
    private RedisService redisService;

    /**
     * 获取用户信息
     * @param model
     * @param user
     * @return
     */
    @RequestMapping("/info")
    @ResponseBody
    public Result<SksUser> info(Model model, SksUser user) {
        // 因为之前写了参数解析器，user已经被直接赋值给参数了，这里直接返回就行了
        return Result.success(user);
    }
}
