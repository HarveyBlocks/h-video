package com.harvey.hvideo.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
/**
 * 滚动查询
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 14:05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScrollResult {
    private List<?> list;
    private Long minTime;
    private Integer offset;
}
