package pers.cy.speedkillsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import pers.cy.speedkillsystem.domain.User;
import pers.cy.speedkillsystem.rabbitmq.MQSender;
import pers.cy.speedkillsystem.redis.RedisService;
import pers.cy.speedkillsystem.redis.UserKey;
import pers.cy.speedkillsystem.result.CodeMsg;
import pers.cy.speedkillsystem.result.Result;
import pers.cy.speedkillsystem.service.UserService;

@Controller
@RequestMapping("/demo")
public class SampleController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MQSender mqSender;

    @RequestMapping("/thymeleaf")
    public String thymeleaf(Model model) {
        model.addAttribute("name", "cy");
        return "hello";
    }

    @RequestMapping("/hello")
    // 需要返回对象的时候就要使用这个注释，一般在异步的时候使用，如果只是返回模板界面，就不需要了
    @ResponseBody
    public Result<String> hello() {
        return Result.success("hello world");
        // return new Result(0, "success", "hello world");
    }

    /*
    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> mq() {
        mqSender.send("hello");
        return Result.success("hello world");
    }

    @RequestMapping("/mq/topic")
    @ResponseBody
    public Result<String> topic() {
        mqSender.sendTopic("hello");
        return Result.success("hello world");
    }

    @RequestMapping("/mq/fanout")
    @ResponseBody
    public Result<String> fanout() {
        mqSender.sendFanout("hello");
        return Result.success("hello world");
    }

    @RequestMapping("/mq/header")
    @ResponseBody
    public Result<String> header() {
        mqSender.sendHeader("hello");
        return Result.success("hello world");
    }

     */

    @RequestMapping("/helloError")
    @ResponseBody
    public Result<String> helloError() {
        return Result.error(CodeMsg.SERVER_ERROR);
        // return new Result(500102,"xxx");
    }

    @RequestMapping("/db/tx")
    @ResponseBody
    public Result<Boolean> dbTx() {
        // 执行事务
        userService.tx();
        return Result.success(true);
    }

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet() {
        // 通过自己编写的redisService进行操作  通过key值取得redis中的数据，并将其转化为指定的类型进行返回，这里就是将key3的数据转换成Long类型返回
        User user = redisService.get(UserKey.getById, "" + 1, User.class);
        // 将该数据返回界面
        return Result.success(user);
    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet() {
        User user = new User();
        user.setId(1);
        user.setName("1111");
        redisService.set(UserKey.getById, "1", user); // UserKey:id1
//        // 通过自己编写的redisService进行操作  通过key值取得redis中的数据，并将其转化为指定的类型进行返回，这里就是将key3的数据转换成Long类型返回
//        Boolean v1 = redisService.set(UserKey.getById, "key2", "hello world");
//        String str = redisService.get("key2", String.class);
        // 将该数据返回界面
        return Result.success(true);
    }
}
