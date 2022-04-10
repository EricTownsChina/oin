package priv.eric.oin.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Desc:
 *
 * @author EricTownsChina@outlook.com
 * create 2022/4/10 0:58
 */
@Setter
@Getter
@Builder
@AllArgsConstructor
public class BaseDoc {

    private String code;

    private Long createTime;

    private Long updateTime;

    private Integer status;

    public BaseDoc() {
        long now = System.currentTimeMillis();
        this.createTime = now;
        this.updateTime = now;
        this.status = DocStatusEnum.VALID.code;
    }

}
