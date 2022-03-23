package priv.eric.oin.service;

import java.io.File;
import java.util.Collection;
import java.util.Deque;

/**
 * @author EricTownsChina@outlook.com
 * create 2022/2/12 22:35
 * <p>
 * Desc: BV视频弹幕相关service
 */
public interface BvCommentService {

    /**
     * 根据BV号获取视频弹幕文件
     *
     * @param bvCode BV号
     * @return 弹幕文件
     */
    void getCommentByBvCode(String bvCode);

    /**
     * 重载接口
     * {@link #getCommentByBvCode(String)}
     *
     * @param bvCodes BV号集合
     * @return 弹幕文件
     */
    void getCommentByBvCode(Collection<String> bvCodes);



}
