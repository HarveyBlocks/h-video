package com.harvey.hvideo.pojo.message;

import lombok.Data;
import lombok.ToString;

import java.util.Set;


/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-29 18:58
 */
@Data
@ToString(callSuper = true)
public class GroupMembersMessage  {
    // 获取群聊成员表

    private Set<String> members;

    /**
     * @param members 如果执行错误, members就串null
     * @param reason 对本次执行的描述
     */
    public GroupMembersMessage(Set<String> members,String reason,long sequenceId) {
        this.members = members;
    }

}
