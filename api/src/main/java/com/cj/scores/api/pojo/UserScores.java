package com.cj.scores.api.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author yuchuanWeng
 * @date 2018/8/27
 * @since 1.0
 */
@Getter
@Setter
public class UserScores implements Serializable {
    private Long uid;

    @JsonProperty("total_scores")
    private Double totalScores;

    private Double scores;

    @JsonProperty("lock_scores")
    private Double lockScores;

    private Boolean enabled;
    /**
     * 1=收入 2=支出 3=冻结 4=解冻
     */
    @NotNull
    private Integer type;

    private String properties;

    @JsonProperty("update_time")
    private String updateTime;

    @JsonProperty("create_time")
    private String createTime;

    private Long version;
}
