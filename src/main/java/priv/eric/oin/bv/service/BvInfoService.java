package priv.eric.oin.bv.service;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Desc: 视频信息获取service
 *
 * @author EricTownsChina@outlook.com
 * create 2022/3/23 21:33
 */
public interface BvInfoService {

    /**
     * 获取视频 未处理的基本信息
     *
     * @param bvid 视频bvid
     * @return String
     */
    String getBvBaseInfoStr(String bvid);

    /**
     * 获取一个网页中所有的BV号
     *
     * @param url b站网页
     * @return BV号列表
     */
    Set<String> getBvCodesByUrl(String url);

    /**
     * 存储bv视频信息
     *
     * @param bvid bvid
     */
    void send(String bvid);

    /**
     * 批量获取视频的基本信息
     *
     * @param bvids 视频bvid列表
     * @return List<Map < String, Object>>
     */
    List<Map<String, Object>> bulkBvBaseInfo(List<String> bvids);

}
