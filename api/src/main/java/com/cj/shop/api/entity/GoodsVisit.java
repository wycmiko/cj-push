package com.cj.shop.api.entity;

import com.cj.shop.common.model.PropertyEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
@Getter
@Setter
public class GoodsVisit extends PropertyEntity implements Serializable {
    private Long id;

    private Long uid;

    private Long goodsId;

    private String visitTime;

    private String createTime;

    private String properties;
}