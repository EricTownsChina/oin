package priv.eric.oin.service;

import priv.eric.oin.entity.Bv;

/**
 * @author EricTownsChina@outlook.com
 * create 2022/2/12 22:30
 * <p>
 * Desc: BV视频相关service
 */
public interface BvService {

    /**
     * 根据BV号获取BV信息
     *
     * @param bvCode BV号
     * @return BV视频信息
     */
    Bv getBvByCode(String bvCode);

}
