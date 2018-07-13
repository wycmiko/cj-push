package com.cj.shop.api.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author yuchuanWeng( )
 * @date 2018/7/12
 * @since 1.0
 */
@Getter
@Setter
public class PayRequest {
    /**
     * 分配的id
     */
    @JsonProperty("app_id")
    private String appId;
    /**
     * 商户订单号
     */
    @JsonProperty("out_trade_no")
    private String outTradeNo;
    /**
     * 支付通道 1=支付宝 2=微信
     */
    @JsonProperty("pay_channel")
    private String payChannel;
    /**
     * 卖家ID
     */
    @JsonProperty("seller_id")
    private String sellerId;
    /**
     * 买家uid
     */
    @JsonProperty("buyer_id")
    private String buyerId;
    /**
     * 商品ID
     */
    @JsonProperty("product_id")
    private String productId;
    /**
     * 描述
     */
    private String body;

    private String ip;

    private String properties;


}
