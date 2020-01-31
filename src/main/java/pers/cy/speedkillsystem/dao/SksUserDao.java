package pers.cy.speedkillsystem.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import pers.cy.speedkillsystem.domain.SksUser;

@Mapper
public interface SksUserDao {
    @Select("select * from sks_user where id = #{id}")
    public SksUser getById(@Param("id") long id);

    /**
     * 只传了要更新的东西，这样的执行效率是最快的
     * @param toBeUpdate
     */
    @Update("update sks_user set password = #{password} where id = #{id}")
    public void update(SksUser toBeUpdate);
}
