package priv.eric.oin.entity;

/**
 * @author 赵元路 18358572500
 * <p>
 * Description: 基础返回码和返回信息定义
 * @date 2020/3/12 12:50
 */
public enum ResponseCode {
    /**
     * 响应码
     */
    SUCCESS(0, "OK"),
    FAIL(-1, "ERROR");


    private final Integer code;
    private final String desc;

    ResponseCode(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ResponseCode getEnumByCode(Integer code) {
        for (ResponseCode rce : ResponseCode.values()) {
            if (rce.code.equals(code)) {
                return rce;
            }
        }
        return null;
    }

}
