package com.cj.shop.api.entity;

import com.cj.shop.common.model.PropertyEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class GoodsCommentWithBLOBs extends PropertyEntity implements Serializable {
    private Long id;

    private Long shopId;

    private Long goodsId;

    private Long orderId;

    private Long uid;

    private Double goodsScore;

    private Double serverScore;

    private Double expressScore;

    private Integer commentType;

    private Integer showFlag;

    private Integer deleteFlag;

    private String updateTime;

    private String createTime;

    private Integer sortFlag;
    /**
     * text
     */
    private String content;

}