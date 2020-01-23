package pers.cy.speedkillsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pers.cy.speedkillsystem.dao.UserDao;
import pers.cy.speedkillsystem.domain.User;

@Service
public class UserService {
    @Autowired
    private UserDao userDao;

    public User getById(int id) {
        return userDao.getById(id);
    }

    // 添加事务标签，将这一段进行事务管理，如果中途失败整个事务都会回滚
    @Transactional
    public boolean tx() {
        User u1 = new User();
        u1.setId(2);
        u1.setName("hy");
        userDao.insert(u1);

        User u2 = new User();
        u2.setId(1);
        u2.setName("cc");
        userDao.insert(u2);

        return true;
    }
}
