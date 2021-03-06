package priv.eric.oin.bv.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.*;
import priv.eric.oin.common.entity.BaseDoc;

/**
 * Desc: bv信息ES doc实体
 *
 * @author EricTownsChina@outlook.com
 * create 2022/3/27 21:22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BvInfoDoc extends BaseDoc {

    /**
     * 源数据
     */
    private String dataSource;
    /**
     * bv号
     */
    private String bvid;
    /**
     * bv参与的活动ID
     */
    @JSONField(name = "mission_id")
    private Integer missionId;
    /**
     * 稿件ID
     */
    private Integer aid;
    /**
     * bv封面
     */
    private String pic;
    /**
     * bv分P数
     */
    private Integer videos;
    /**
     * bv标题
     */
    private String title;
    /**
     * 分区ID
     */
    private Integer tid;
    /**
     * 分区名称
     */
    @JSONField(name = "tname")
    private String tName;
    /**
     * 总时长
     */
    private Integer duration;
    /**
     * 视频简介
     */
    private String desc;
    /**
     * 投稿时间
     */
    @JSONField(name = "ctime")
    private Long cTime;
    /**
     * 稿件发布时间
     */
    @JSONField(name = "pubdate")
    private Long pubDate;
    /**
     * 作者头像
     */
    private String ownerFace;
    /**
     * 作者mid
     */
    private Integer ownerMid;
    /**
     * 作者昵称
     */
    private String ownerName;
    /**
     * 喜欢数
     */
    private Long like;
    /**
     * 不喜欢数, 恒为0
     */
    private Long dislike;
    /**
     * 播放数
     */
    private Long view;
    /**
     * 弹幕数
     */
    private Long danmaku;
    /**
     * 分享数
     */
    private Long share;
    /**
     * 评论数
     */
    private Long reply;
    /**
     * 收藏数
     */
    private Long favorite;
    /**
     * 硬币数
     */
    private Long coin;
    /**
     * 当前排名
     */
    private Long nowRank;
    /**
     * 历史最高排名
     */
    private Long hisRank;
    /**
     * 警告信息
     */
    @JSONField(name = "argue_msg")
    private String argueMsg;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
