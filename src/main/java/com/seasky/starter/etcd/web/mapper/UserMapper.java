package com.seasky.starter.etcd.web.mapper;

import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserMapper {

    /*@Select("select * from user where user_name=#{userName}")*/
   /* public User getUserByUserName(@Param("userName") String userName);

    @Delete("delete from user where id=#{id}")
    public int deleteUserById(Integer id);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into user(user_name,password,date) values(#{userName},#{password},now())")
    public int insertUser(User user);

    @Update("update user set user_name=#{userName} where id=#{id}")
    public int updateUser(User user);*/
}
