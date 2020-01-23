package pers.cy.speedkillsystem.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import pers.cy.speedkillsystem.domain.User;

// 下面就是mybatis的注解，所有的数据库操作接口都要加这个注解
@Mapper
public interface UserDao {
    // 正常来说应该是一个一个接口匹配一个XML，但是我们用下面这个写法就不需要写XML了
    @Select("select * from user where id = #{id}")
    public User getById(@Param("id") int id); // 使用@Param直接，将方法参数中的id和上面注解的id匹配，自动将传入参数的id值赋值给注解中的id

    // Mybatis会自动将注解总的属性和参数中User对象中同名属性关联起来
    @Insert("insert into user(id, name) values(#{id}, #{name})")
    public int insert(User user);
}
