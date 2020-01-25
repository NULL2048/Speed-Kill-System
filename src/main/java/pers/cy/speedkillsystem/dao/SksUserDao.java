package pers.cy.speedkillsystem.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import pers.cy.speedkillsystem.domain.SksUser;

@Mapper
public interface SksUserDao {
    @Select("select * from sks_user where id = #{id}")
    public SksUser getById(@Param("id") long id);
}
