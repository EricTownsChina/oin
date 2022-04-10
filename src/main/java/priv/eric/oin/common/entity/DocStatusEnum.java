package priv.eric.oin.common.entity;

/**
 * Desc:
 *
 * @author EricTownsChina@outlook.com
 * create 2022/4/10 1:17
 */
public enum DocStatusEnum {

    INVALIDATE(-1, "无效"),
    VALID(1, "有效"),
    UPDATE(0, "更新中");

    int code;
    String desc;

    DocStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
