package com.harvey.hvideo.pojo.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 角色(用于授权)
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 16:09
 */
@Getter
public enum Role {
    ROOT(0,"管理员"),NORMAL(1,"普通");

    @EnumValue
    private final Integer value;// 这里由于之前创建表的时候的限制, 以后还是用int
    private final String desc;// description


    Role(Integer value,String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static Role create(Integer roleValue){
        Role role;
        switch (roleValue){
            case 0:role = ROOT;break;
            case 1:role = NORMAL;break;
            default:role = NORMAL;
        }
        return role;
    }


}
