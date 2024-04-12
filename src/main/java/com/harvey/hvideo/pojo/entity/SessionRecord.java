package com.harvey.hvideo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-11 14:28
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("`session_record`")
public class SessionRecord {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private Long user1;
    private Long user2;
    public SessionRecord(long user1, long user2){
        this.user1 = user1;
        this.user2 = user2;
    }
}
