package priv.eric.oin.entity;

import lombok.Data;

import java.sql.Timestamp;

/**
 * @author EricTownsChina@outlook.com
 * @date 2021-12-16 23:13
 * <p>
 * desc: BV 弹幕实体类
 */
@Data
public class BvComment {

    /**
     * ID
     */
    private long id;
    /**
     * 弹幕ID
     */
    private long commentId;
    /**
     * bv code
     */
    private String bvCode;
    /**
     * bv cid
     */
    private String cid;
    /**
     * bv位置偏移量
     */
    private String offset;
    /**
     * 弹幕内容
     */
    private String content;
    /**
     * 获取时间
     */
    private Timestamp createTime;

}
