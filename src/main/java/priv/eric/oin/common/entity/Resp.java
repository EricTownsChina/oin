package priv.eric.oin.common.entity;

import lombok.Data;
import org.springframework.util.StringUtils;

import static priv.eric.oin.common.entity.ResponseCode.*;

/**
 * @author Eric 840017241@qq.com
 * Create 2021-08-26 23:17
 * <p>
 * desc: 统一响应实体类
 */
@Data
public class Resp {

    /**
     * 响应编码
     */
    private Integer code;

    /**
     * 响应信息
     */
    private String msg;

    /**
     * 响应数据
     */
    private Object data;

    public static Builder n() {
        return new Builder();
    }

    public static class Builder {
        private Integer code;
        private String msg;
        private Object data;

        public Builder setCode(Integer code) {
            this.code = code;
            return this;
        }

        public Builder setMsg(String msg) {
            this.msg = msg;
            return this;
        }

        public Builder setData(Object data) {
            this.data = data;
            return this;
        }

        public Resp build() {
            Resp resp = new Resp();
            resp.code = this.code;
            resp.msg = this.msg;
            resp.data = this.data;
            return resp;
        }
    }

    public static Resp ok() {
        return Resp.n()
                .setCode(SUCCESS.getCode())
                .setMsg(SUCCESS.getDesc())
                .build();
    }

    public static Resp ok(Object data) {
        return Resp.n()
                .setCode(SUCCESS.getCode())
                .setMsg(SUCCESS.getDesc())
                .setData(data)
                .build();
    }

    public static Resp fail() {
        return Resp.n()
                .setCode(FAIL.getCode())
                .setMsg(FAIL.getDesc())
                .build();
    }

    public static Resp fail(String msg) {
        return Resp.n()
                .setCode(FAIL.getCode())
                .setMsg(StringUtils.isEmpty(msg) ? FAIL.getDesc() : msg)
                .build();
    }

    public static Resp fail(Integer code, String msg) {
        return Resp.n()
                .setCode(code)
                .setMsg(StringUtils.isEmpty(msg) ? FAIL.getDesc() : msg)
                .build();
    }

}
