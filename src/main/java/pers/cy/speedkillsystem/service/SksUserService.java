package pers.cy.speedkillsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pers.cy.speedkillsystem.dao.SksUserDao;
import pers.cy.speedkillsystem.domain.SksUser;
import pers.cy.speedkillsystem.exception.GlobalException;
import pers.cy.speedkillsystem.result.CodeMsg;
import pers.cy.speedkillsystem.result.Result;
import pers.cy.speedkillsystem.util.MD5Util;
import pers.cy.speedkillsystem.vo.LoginVo;

@Service
public class SksUserService {
    @Autowired
    private SksUserDao sksUserDao;

    /**
     * 通过ID查询用户
     * @param id
     * @return
     */
    public SksUser getById(long id) {
        return sksUserDao.getById(id);
    }

    /**
     * 一般在开发中登录模块都是写在用户的service中的，因为是用户要登陆，所以不要单独写一个loginService了
     * 用户登陆操作
     * @param loginVo
     */
    public boolean login(LoginVo loginVo) {
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
        return true;
    }
}
