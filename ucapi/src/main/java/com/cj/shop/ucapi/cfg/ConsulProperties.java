package com.cj.shop.ucapi.cfg;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>Create Time: 2018年03月22日</p>
 * <p>@author tangxd</p>
 **/
@Component
@ConfigurationProperties(prefix = "spring.cloud.consul")
@Getter
@Setter
public class ConsulProperties {
    private String host;
    private int port;
}
