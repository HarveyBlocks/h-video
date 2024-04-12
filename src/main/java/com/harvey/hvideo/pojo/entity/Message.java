package com.harvey.hvideo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 消息文本信息
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-12 13:58
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("`tb_message`")
@NoArgsConstructor
public class Message {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private Long fromId;
    private String content;
    private Long sessionId;
    private Long groupId;

    public Message(Long fromId, String content,Long sessionId,Long groupId) {
        this.fromId = fromId;
        this.content = content;
        this.sessionId = sessionId;
        this.groupId = groupId;
    }
}
