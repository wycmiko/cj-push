package com.cj.shop.api.entity;

import com.cj.shop.common.model.PropertyEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GoodsSpecWithBLOBs extends PropertyEntity implements Serializable {
    private Long id;
    @JsonProperty("parent_id")
    private Long parentId;
    @JsonProperty("parent_name")
    private String parentName;
    @JsonProperty("spec_name")
    private String specName;
    @JsonProperty("delete_flag")
    private Integer deleteFlag;
    @JsonProperty("update_time")
    private String updateTime;
    @JsonProperty("create_time")
    private String createTime;
    @JsonProperty("sort_flag")
    private Integer sortFlag;
    //子菜单列表
    @JsonProperty("sub_list")
    private List<GoodsSpecWithBLOBs> subList = new ArrayList<>();
}