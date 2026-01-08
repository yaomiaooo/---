package com.example.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hospital.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * User Mapper 接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据手机号查询用户
     * @param userPhone 手机号
     * @return 用户实体
     */
    @Select("SELECT * FROM user WHERE user_phone = #{userPhone}")
    User selectByUserPhone(String userPhone);

}
