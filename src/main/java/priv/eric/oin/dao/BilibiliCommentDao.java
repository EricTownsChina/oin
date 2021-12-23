package priv.eric.oin.dao;

import priv.eric.oin.entity.BvComment;

import java.util.List;
import java.util.Set;

/**
 * @author EricTownsChina@outlook.com
 * @date 2021-12-17 15:20
 * <p>
 * desc:
 */
public interface BilibiliCommentDao {

    /**
     * 添加新的BV弹幕
     *
     * @param bvComment BV弹幕
     */
    void addNewComment(BvComment bvComment);

    /**
     * 批量添加新的BV弹幕
     *
     * @param bvCommentList BV弹幕列表
     */
    void batchAddNewComment(List<BvComment> bvCommentList);

    /**
     * 获取所有的BV号
     *
     * @return Set
     */
    Set<String> selectAllBvCodes();

}
