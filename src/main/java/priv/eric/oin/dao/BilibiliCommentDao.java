package priv.eric.oin.dao;

import priv.eric.oin.entity.BvComment;

/**
 * @author EricTownsChina@outlook.com
 * @date 2021-12-17 15:20
 * <p>
 * desc:
 */
public interface BilibiliCommentDao {

    /**
     * 添加新的BV弹幕
     * @param bvComment BV弹幕
     */
    void addNewComment(BvComment bvComment);

}
